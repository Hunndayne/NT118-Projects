package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.AssignmentCreateRequest;
import com.finalproject.backend.dto.request.AssignmentUpdateRequest;
import com.finalproject.backend.dto.response.AssignmentResponse;
import com.finalproject.backend.entity.Assignment;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.AssignmentRepository;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

	private static final DateTimeFormatter[] LOCAL_DATE_TIME_FORMATTERS = new DateTimeFormatter[]{
			DateTimeFormatter.ISO_LOCAL_DATE_TIME,
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
	};
	private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

	private final AssignmentRepository assignmentRepository;
	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final UserService userService;

	@Transactional(readOnly = true)
	public List<AssignmentResponse> getAssignments(String token, Long classId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireAccessToClass(requester, clazz);

		return assignmentRepository.findByClazz_IdOrderByDeadlineAsc(clazz.getId())
				.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public AssignmentResponse getAssignment(String token, Long classId, Long assignmentId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireAccessToClass(requester, clazz);

		Assignment assignment = assignmentRepository.findByIdAndClazz_Id(assignmentId, clazz.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
		return toResponse(assignment);
	}

	@Transactional
	public AssignmentResponse createAssignment(String token, Long classId, AssignmentCreateRequest request) {
		User creator = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(creator, clazz);

		String title = trimRequired(request.getTitle(), "title");
		OffsetDateTime deadline = parseDateTime(request.getDeadline());
		OffsetDateTime startTime = parseDateTime(request.getStartTime());

		Assignment assignment = Assignment.builder()
				.clazz(clazz)
				.title(title)
				.description(trimToNull(request.getDescription()))
				.attachmentUrl(trimToNull(request.getAttachmentUrl()))
				.deadline(deadline)
				.createdBy(creator)
				.createdAt(startTime != null ? startTime.toInstant() : null)
				.build();

		Assignment saved = assignmentRepository.save(assignment);
		return toResponse(saved);
	}

	@Transactional
	public AssignmentResponse updateAssignment(String token, Long classId, Long assignmentId, AssignmentUpdateRequest request) {
		User updater = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(updater, clazz);

		Assignment assignment = assignmentRepository.findByIdAndClazz_Id(assignmentId, clazz.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

		if (request.getTitle() != null) {
			assignment.setTitle(trimRequired(request.getTitle(), "title"));
		}
		if (request.getDescription() != null) {
			assignment.setDescription(trimToNull(request.getDescription()));
		}
		if (request.getAttachmentUrl() != null) {
			assignment.setAttachmentUrl(trimToNull(request.getAttachmentUrl()));
		}
		if (request.getDeadline() != null) {
			assignment.setDeadline(parseDateTime(request.getDeadline()));
		}
		if (request.getStartTime() != null) {
			OffsetDateTime startTime = parseDateTime(request.getStartTime());
			assignment.setCreatedAt(startTime != null ? startTime.toInstant() : null);
		}

		Assignment saved = assignmentRepository.save(assignment);
		return toResponse(saved);
	}

	@Transactional
	public void deleteAssignment(String token, Long classId, Long assignmentId) {
		User deleter = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(deleter, clazz);

		Assignment assignment = assignmentRepository.findByIdAndClazz_Id(assignmentId, clazz.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
		assignmentRepository.delete(assignment);
	}

	private AssignmentResponse toResponse(Assignment assignment) {
		Long classId = assignment.getClazz() != null ? assignment.getClazz().getId() : null;
		Long createdBy = assignment.getCreatedBy() != null ? assignment.getCreatedBy().getId() : null;
		Instant createdAt = assignment.getCreatedAt();
		return new AssignmentResponse(
				assignment.getId(),
				classId,
				assignment.getTitle(),
				trimToNull(assignment.getDescription()),
				trimToNull(assignment.getAttachmentUrl()),
				assignment.getDeadline(),
				assignment.getWeight(),
				createdBy,
				createdAt
		);
	}

	private ClassEntity resolveClass(Long classIdOrCourseId) {
		ClassEntity clazz = classRepository.findById(classIdOrCourseId)
				.orElseGet(() -> classRepository.findFirstByCourse_Id(classIdOrCourseId));
		if (clazz == null) {
			Course course = courseRepository.findById(classIdOrCourseId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
			clazz = ClassEntity.builder()
					.name(course.getName())
					.description(course.getDescription())
					.active(course.getActive())
					.course(course)
					.createdBy(course.getCreatedBy())
					.build();
			clazz = classRepository.save(clazz);
		}
		clazz.getTeachers().size();
		return clazz;
	}

	private void requireAccessToClass(User user, ClassEntity clazz) {
		if (user.isSuperAdmin()) {
			return;
		}
		boolean isTeacher = clazz.getTeachers().stream().anyMatch(t -> t.getId().equals(user.getId()));
		if (user.isTeacher() && isTeacher) {
			return;
		}
		if (user.isTeacher() && isUserInCourse(user, clazz)) {
			return;
		}
		if (user.isStudent() && isUserInCourse(user, clazz)) {
			return;
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this class");
	}

	private void requireTeacherOrAdmin(User user, ClassEntity clazz) {
		if (user.isSuperAdmin()) {
			return;
		}
		boolean isTeacher = clazz.getTeachers().stream().anyMatch(t -> t.getId().equals(user.getId()));
		if (user.isTeacher() && isTeacher) {
			return;
		}
		if (user.isTeacher() && isUserInCourse(user, clazz)) {
			return;
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher or super_admin required for this class");
	}

	private boolean isUserInCourse(User user, ClassEntity clazz) {
		if (clazz.getCourse() == null || clazz.getCourse().getId() == null) {
			return false;
		}
		return courseRepository.findByIdAndStudents_Id(clazz.getCourse().getId(), user.getId()).isPresent();
	}

	private OffsetDateTime parseDateTime(String value) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return null;
		}
		try {
			return OffsetDateTime.parse(trimmed, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		} catch (DateTimeParseException ignored) {
			// try next
		}
		for (DateTimeFormatter formatter : LOCAL_DATE_TIME_FORMATTERS) {
			try {
				LocalDateTime localDateTime = LocalDateTime.parse(trimmed, formatter);
				return localDateTime.atZone(DEFAULT_ZONE).toOffsetDateTime();
			} catch (DateTimeParseException ignored) {
				// try next
			}
		}
		try {
			LocalDate localDate = LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
			return localDate.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
		} catch (DateTimeParseException ignored) {
			// try next
		}
		try {
			LocalDate localDate = LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			return localDate.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
		} catch (DateTimeParseException ignored) {
			// try next
		}
		try {
			LocalDate localDate = LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			return localDate.atStartOfDay(DEFAULT_ZONE).toOffsetDateTime();
		} catch (DateTimeParseException ignored) {
			// try next
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date time format");
	}

	private String trimRequired(String value, String fieldName) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
		}
		return trimmed;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
