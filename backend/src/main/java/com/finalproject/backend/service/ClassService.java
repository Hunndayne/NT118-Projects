package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.ClassCreateRequest;
import com.finalproject.backend.dto.request.ClassTeachersRequest;
import com.finalproject.backend.dto.request.ClassUpdateRequest;
import com.finalproject.backend.dto.response.ClassResponse;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final UserService userService;

	@Transactional
	public ClassResponse createClass(String rawToken, ClassCreateRequest request) {
		User creator = userService.getAuthenticatedUserEntity(rawToken);
		requireSuperAdmin(creator);

		Course course = loadCourse(request.getCourseId());

		ClassEntity entity = ClassEntity.builder()
				.name(request.getName().trim())
				.description(trimToNull(request.getDescription()))
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.active(request.getActive() != null ? request.getActive() : Boolean.TRUE)
				.course(course)
				.createdBy(creator)
				.build();

		ClassEntity saved = classRepository.save(entity);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<ClassResponse> getClassesForCurrentUser(String rawToken) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		List<ClassEntity> classes;
		if (requester.isSuperAdmin()) {
			classes = classRepository.findAll();
		} else if (requester.isTeacher()) {
			classes = classRepository.findDistinctByTeachers_Id(requester.getId());
		} else {
			classes = new ArrayList<>();
		}
		return classes.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ClassResponse getClassForCurrentUser(String rawToken, Long classId) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		ClassEntity entity = loadClass(classId);
		if (requester.isSuperAdmin()) {
			return toResponse(entity);
		}
		if (requester.isTeacher() && entity.getTeachers().stream().anyMatch(t -> t.getId().equals(requester.getId()))) {
			return toResponse(entity);
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this class");
	}

	@Transactional
	public ClassResponse updateClass(String rawToken, Long classId, ClassUpdateRequest request) {
		User updater = userService.getAuthenticatedUserEntity(rawToken);
		requireSuperAdmin(updater);

		ClassEntity entity = loadClass(classId);

		if (request.getName() != null) {
			entity.setName(request.getName().trim());
		}
		if (request.getDescription() != null) {
			entity.setDescription(trimToNull(request.getDescription()));
		}
		if (request.getStartDate() != null) {
			entity.setStartDate(request.getStartDate());
		}
		if (request.getEndDate() != null) {
			entity.setEndDate(request.getEndDate());
		}
		if (request.getActive() != null) {
			entity.setActive(request.getActive());
		}

		ClassEntity saved = classRepository.save(entity);
		return toResponse(saved);
	}

	@Transactional
	public void deleteClass(String rawToken, Long classId) {
		User deleter = userService.getAuthenticatedUserEntity(rawToken);
		requireSuperAdmin(deleter);

		ClassEntity entity = loadClass(classId);
		classRepository.delete(entity);
	}

	@Transactional
	public ClassResponse addTeachers(String rawToken, Long classId, ClassTeachersRequest request) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		requireSuperAdmin(admin);

		ClassEntity entity = loadClassWithTeachers(classId);
		Set<User> teachers = new HashSet<>(entity.getTeachers());

		for (Long teacherId : request.getTeacherIds()) {
			if (teacherId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teacherId must not be null");
			}
			User teacher = userService.loadUserEntity(teacherId);
			if (!teacher.isTeacher() && !teacher.isSuperAdmin()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User " + teacherId + " không phải giáo viên/super_admin");
			}
			boolean added = teachers.add(teacher);
			if (!added) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher " + teacherId + " đã tồn tại trong lớp");
			}
		}

		entity.setTeachers(teachers);
		ClassEntity saved = classRepository.save(entity);
		return toResponse(saved);
	}

	@Transactional
	public ClassResponse removeTeachers(String rawToken, Long classId, ClassTeachersRequest request) {
		User admin = userService.getAuthenticatedUserEntity(rawToken);
		requireSuperAdmin(admin);

		ClassEntity entity = loadClassWithTeachers(classId);
		Set<User> teachers = new HashSet<>(entity.getTeachers());

		for (Long teacherId : request.getTeacherIds()) {
			if (teacherId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teacherId must not be null");
			}
			User teacher = userService.loadUserEntity(teacherId);
			boolean removed = teachers.remove(teacher);
			if (!removed) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher " + teacherId + " không có trong lớp");
			}
		}

		entity.setTeachers(teachers);
		ClassEntity saved = classRepository.save(entity);
		return toResponse(saved);
	}

	private Course loadCourse(Long courseId) {
		return courseRepository.findById(courseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
	}

	private ClassEntity loadClass(Long classId) {
		return classRepository.findById(classId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
	}

	private ClassEntity loadClassWithTeachers(Long classId) {
		return classRepository.findById(classId)
				.map(cls -> {
					cls.getTeachers().size(); // init
					return cls;
				})
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
	}

	private void requireSuperAdmin(User user) {
		if (!user.isSuperAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin privileges required");
		}
	}

	private ClassResponse toResponse(ClassEntity entity) {
		List<Long> teacherIds = entity.getTeachers().stream()
				.map(User::getId)
				.collect(Collectors.toList());

		Long courseId = entity.getCourse() != null ? entity.getCourse().getId() : null;
		Long createdBy = entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null;

		return ClassResponse.builder()
				.id(entity.getId())
				.name(entity.getName())
				.description(trimToNull(entity.getDescription()))
				.startDate(entity.getStartDate())
				.endDate(entity.getEndDate())
				.active(entity.getActive())
				.courseId(courseId)
				.createdBy(createdBy)
				.createdAt(entity.getCreatedAt())
				.teacherIds(teacherIds)
				.build();
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
