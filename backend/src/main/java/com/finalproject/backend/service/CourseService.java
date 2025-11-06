package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.CourseCreationRequest;
import com.finalproject.backend.dto.response.CourseResponse;
import com.finalproject.backend.dto.request.CourseUpdateRequest;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.CourseRepository;
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
	private final UserService userService;

	@Transactional
	public CourseResponse createCourse(String rawToken, CourseCreationRequest request) {
		User creator = userService.getAuthenticatedUserEntity(rawToken);
		if (!creator.isAdmin()) {
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
	public List<CourseResponse> getCoursesForCurrentUser(String rawToken) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		List<Course> courses;
		if (requester.isAdmin()) {
			courses = courseRepository.findAll();
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
		if (!admin.isAdmin()) {
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
		if (!admin.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
		}

		Course course = loadCourse(courseId);
		courseRepository.delete(course);
	}

	private CourseResponse toResponse(Course course) {
		Long creatorId = course.getCreatedBy() != null ? course.getCreatedBy().getId() : null;
		return new CourseResponse(
				course.getId(),
				course.getCode(),
				course.getName(),
				trimToNull(course.getLevel()),
				trimToNull(course.getDescription()),
				course.getActive(),
				creatorId,
				course.getCreatedAt()
		);
	}

	private Course loadCourse(Long courseId) {
		return courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}

	private Course ensureCourseAccess(User requester, Long courseId) {
		if (requester.isAdmin()) {
			return loadCourse(courseId);
		}
		return courseRepository.findByIdAndStudents_Id(courseId, requester.getId())
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
