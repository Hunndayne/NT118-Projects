package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.CourseCreationRequest;
import com.finalproject.backend.dto.response.CourseResponse;
import com.finalproject.backend.dto.response.CourseParticipantResponse;
import com.finalproject.backend.dto.request.CourseUpdateRequest;
import com.finalproject.backend.dto.request.CourseParticipantsRequest;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.CourseRepository;
import com.finalproject.backend.repository.LessonRepository;
import com.finalproject.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

	private final CourseRepository courseRepository;
	private final LessonRepository lessonRepository;
	private final UserService userService;
	private final UserRepository userRepository;

	@Transactional
	public CourseResponse createCourse(String rawToken, CourseCreationRequest request) {
		User creator = userService.getAuthenticatedUserEntity(rawToken);
		if (!creator.isSuperAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		String code = trimRequired(request.getCode(), "code");
		String name = trimRequired(request.getName(), "name");
		String level = trimToNull(request.getLevel());
		Boolean active = request.getActive();
		if (active == null) {
			active = Boolean.TRUE;
		}
		String description = trimToNull(request.getDescription());

		if (courseRepository.existsByCodeIgnoreCase(code)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Course code already exists");
		}

		Course course = Course.builder()
				.code(code)
				.name(name)
				.level(level)
				.active(active)
				.description(description)
				.createdBy(creator)
				.build();

		Course saved = courseRepository.save(course);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public CourseResponse getCourseForCurrentUser(String rawToken, Long courseId) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		Course course = ensureCourseAccess(requester, courseId);
		return toResponse(course);
	}

	@Transactional(readOnly = true)
	public List<CourseParticipantResponse> getParticipantsForCourse(String rawToken, Long courseId) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		Course course = ensureCourseAccess(requester, courseId);
		
		// Combine students and teachers
		List<CourseParticipantResponse> participants = new java.util.ArrayList<>();
		participants.addAll(course.getStudents().stream()
				.map(this::toParticipantResponse)
				.collect(Collectors.toList()));
		participants.addAll(course.getTeachers().stream()
				.map(this::toParticipantResponse)
				.collect(Collectors.toList()));
		return participants;
	}

	@Transactional(readOnly = true)
	public List<CourseParticipantResponse> getEligibleParticipants(String rawToken, Long courseId) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		if (!admin.isSuperAdmin() && !admin.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		loadCourse(courseId);
		List<User> candidates = userRepository.findActiveUsersNotInCourse(courseId);
		return candidates.stream()
				.map(this::toParticipantResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<CourseResponse> getCoursesForCurrentUser(String rawToken) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		List<Course> courses;
		if (requester.isSuperAdmin()) {
			courses = courseRepository.findAll();
		} else if (requester.isTeacher()) {
			courses = courseRepository.findDistinctByTeachers_Id(requester.getId());
		} else {
			courses = courseRepository.findDistinctByStudents_Id(requester.getId());
		}
		return courses.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public CourseResponse updateCourse(String rawToken, Long courseId, CourseUpdateRequest request) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		if (!admin.isSuperAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		Course course = loadCourse(courseId);

		if (request.getCode() != null) {
			String code = trimRequired(request.getCode(), "code");
			if (!code.equalsIgnoreCase(course.getCode())) {
				Optional<Course> existing = courseRepository.findByCodeIgnoreCase(code);
				if (existing.isPresent() && !existing.get().getId().equals(course.getId())) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Course code already exists");
				}
			}
			course.setCode(code);
		}

		if (request.getName() != null) {
			String name = trimRequired(request.getName(), "name");
			course.setName(name);
		}

		if (request.getLevel() != null) {
			course.setLevel(trimToNull(request.getLevel()));
		}

		if (request.getDescription() != null) {
			course.setDescription(trimToNull(request.getDescription()));
		}

		if (request.getActive() != null) {
			course.setActive(request.getActive());
		}

		Course saved = courseRepository.save(course);
		return toResponse(saved);
	}

	@Transactional
	public void deleteCourse(String rawToken, Long courseId) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		if (!admin.isSuperAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		Course course = loadCourse(courseId);
		courseRepository.delete(course);
	}

	@Transactional
	public CourseResponse addParticipants(String rawToken, Long courseId, CourseParticipantsRequest request) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		if (!admin.isSuperAdmin() && !admin.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		Course course = loadCourseWithParticipants(courseId);
		List<Long> userIds = request.getUserIds();
		if (userIds == null || userIds.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userIds must not be empty");
		}

		for (Long userId : userIds) {
			if (userId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must not be null");
			}
			User participant = userService.loadUserEntity(userId);
			
			// Add to appropriate collection based on role
			if (participant.isTeacher()) {
				boolean added = course.getTeachers().add(participant);
				if (!added) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher " + userId + " already assigned to this course");
				}
			} else {
				boolean added = course.getStudents().add(participant);
				if (!added) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "User " + userId + " already enrolled in this course");
				}
			}
		}

		Course saved = courseRepository.save(course);
		return toResponse(saved);
	}

	@Transactional
	public CourseResponse removeParticipants(String rawToken, Long courseId, CourseParticipantsRequest request) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		if (!admin.isSuperAdmin() && !admin.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		Course course = loadCourseWithParticipants(courseId);
		List<Long> userIds = request.getUserIds();
		if (userIds == null || userIds.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userIds must not be empty");
		}

		for (Long userId : userIds) {
			if (userId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must not be null");
			}
			User participant = userService.loadUserEntity(userId);
			
			// Remove from appropriate collection based on role
			if (participant.isTeacher()) {
				boolean removed = course.getTeachers().remove(participant);
				if (!removed) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher " + userId + " is not assigned to this course");
				}
			} else {
				boolean removed = course.getStudents().remove(participant);
				if (!removed) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + userId + " is not enrolled in this course");
				}
			}
		}

		Course saved = courseRepository.save(course);
		return toResponse(saved);
	}

	private CourseResponse toResponse(Course course) {
		Long creatorId = course.getCreatedBy() != null ? course.getCreatedBy().getId() : null;
		int lessonCount = Math.toIntExact(lessonRepository.countByClazz_Course_Id(course.getId()));
		return new CourseResponse(
				course.getId(),
				course.getCode(),
				course.getName(),
				trimToNull(course.getLevel()),
				trimToNull(course.getDescription()),
				course.getActive(),
				creatorId,
				course.getCreatedAt(),
				lessonCount
		);
	}

	private CourseParticipantResponse toParticipantResponse(User user) {
		return new CourseParticipantResponse(
				user.getId(),
				user.getFirstName(),
				user.getLastName(),
				user.getRole() != null ? user.getRole().name() : null,
				user.isActive()
		);
	}

	private Course loadCourse(Long courseId) {
		return courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}

	private Course loadCourseWithStudents(Long courseId) {
		return courseRepository.findById(courseId)
				.map(course -> {
					course.getStudents().size(); // initialize collection
					return course;
				})
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}

	private Course loadCourseWithParticipants(Long courseId) {
		return courseRepository.findById(courseId)
				.map(course -> {
					course.getStudents().size(); // initialize students
					course.getTeachers().size(); // initialize teachers
					return course;
				})
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}

	private Course ensureCourseAccess(User requester, Long courseId) {
		if (requester.isSuperAdmin()) {
			return loadCourseWithParticipants(courseId);
		}
		
		// Check if user is a teacher in this course
		if (requester.isTeacher()) {
			return courseRepository.findByIdAndTeachers_Id(courseId, requester.getId())
					.map(course -> {
						course.getStudents().size();
						course.getTeachers().size();
						return course;
					})
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this course"));
		}
		
		// Check if user is a student in this course
		return courseRepository.findByIdAndStudents_Id(courseId, requester.getId())
				.map(course -> {
					course.getStudents().size();
					course.getTeachers().size();
					return course;
				})
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this course"));
	}

	private String trimRequired(String value, String fieldName) {
		if (value == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
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
