package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.LessonCreateRequest;
import com.finalproject.backend.dto.request.LessonResourceRequest;
import com.finalproject.backend.dto.request.LessonUpdateRequest;
import com.finalproject.backend.dto.response.LessonResourceResponse;
import com.finalproject.backend.dto.response.LessonResponse;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.Lesson;
import com.finalproject.backend.entity.LessonResource;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.LessonRepository;
import com.finalproject.backend.repository.LessonResourceRepository;
import com.finalproject.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

	private final LessonRepository lessonRepository;
	private final LessonResourceRepository lessonResourceRepository;
	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final UserService userService;

	@Transactional(readOnly = true)
	public List<LessonResponse> getLessons(String token, Long classId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireAccessToClass(requester, clazz);

		Long resolvedClassId = clazz.getId();
		return lessonRepository.findByClazz_IdOrderByOrderIndexAsc(resolvedClassId)
				.stream()
				.map(this::toResponseWithResources)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public LessonResponse getLesson(String token, Long classId, Long lessonId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireAccessToClass(requester, clazz);

		Long resolvedClassId = clazz.getId();
		Lesson lesson = lessonRepository.findByIdAndClazz_Id(lessonId, resolvedClassId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
		return toResponseWithResources(lesson);
	}

	@Transactional
	public LessonResponse createLesson(String token, Long classId, LessonCreateRequest request) {
		User creator = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(creator, clazz);

		Lesson lesson = Lesson.builder()
				.clazz(clazz)
				.title(request.getTitle().trim())
				.description(trimToNull(request.getDescription()))
				.orderIndex(request.getOrderIndex())
				.createdBy(creator)
				.build();

		Lesson saved = lessonRepository.save(lesson);
		return toResponseWithResources(saved);
	}

	@Transactional
	public LessonResponse updateLesson(String token, Long classId, Long lessonId, LessonUpdateRequest request) {
		User updater = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(updater, clazz);

		Long resolvedClassId = clazz.getId();
		Lesson lesson = lessonRepository.findByIdAndClazz_Id(lessonId, resolvedClassId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

		if (request.getTitle() != null) {
			lesson.setTitle(request.getTitle().trim());
		}
		if (request.getDescription() != null) {
			lesson.setDescription(trimToNull(request.getDescription()));
		}
		if (request.getOrderIndex() != null) {
			lesson.setOrderIndex(request.getOrderIndex());
		}

		Lesson saved = lessonRepository.save(lesson);
		return toResponseWithResources(saved);
	}

	@Transactional
	public void deleteLesson(String token, Long classId, Long lessonId) {
		User deleter = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(deleter, clazz);

		Long resolvedClassId = clazz.getId();
		Lesson lesson = lessonRepository.findByIdAndClazz_Id(lessonId, resolvedClassId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
		lessonResourceRepository.deleteByLesson_Id(lesson.getId());
		lessonRepository.delete(lesson);
	}

	@Transactional(readOnly = true)
	public List<LessonResourceResponse> getResources(String token, Long classId, Long lessonId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireAccessToClass(requester, clazz);
		ensureLessonExists(lessonId, clazz.getId());

		return lessonResourceRepository.findByLesson_Id(lessonId)
				.stream()
				.map(this::toResourceResponse)
				.collect(Collectors.toList());
	}

	@Transactional
	public LessonResourceResponse addResource(String token, Long classId, Long lessonId, LessonResourceRequest request) {
		User actor = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(actor, clazz);

		Lesson lesson = ensureLessonExists(lessonId, clazz.getId());

		LessonResource resource = LessonResource.builder()
				.lesson(lesson)
				.type(trimToNull(request.getType()))
				.title(trimToNull(request.getTitle()))
				.content(trimToNull(request.getContent()))
				.url(trimToNull(request.getUrl()))
				.filePath(trimToNull(request.getFilePath()))
				.build();

		LessonResource saved = lessonResourceRepository.save(resource);
		return toResourceResponse(saved);
	}

	@Transactional
	public LessonResourceResponse updateResource(String token, Long classId, Long lessonId, Long resourceId, LessonResourceRequest request) {
		User actor = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(actor, clazz);

		ensureLessonExists(lessonId, clazz.getId());

		LessonResource resource = lessonResourceRepository.findByIdAndLesson_Id(resourceId, lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

		if (request.getType() != null) {
			resource.setType(trimToNull(request.getType()));
		}
		if (request.getTitle() != null) {
			resource.setTitle(trimToNull(request.getTitle()));
		}
		if (request.getContent() != null) {
			resource.setContent(trimToNull(request.getContent()));
		}
		if (request.getUrl() != null) {
			resource.setUrl(trimToNull(request.getUrl()));
		}
		if (request.getFilePath() != null) {
			resource.setFilePath(trimToNull(request.getFilePath()));
		}

		LessonResource saved = lessonResourceRepository.save(resource);
		return toResourceResponse(saved);
	}

	@Transactional
	public void deleteResource(String token, Long classId, Long lessonId, Long resourceId) {
		User actor = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(actor, clazz);

		ensureLessonExists(lessonId, clazz.getId());

		LessonResource resource = lessonResourceRepository.findByIdAndLesson_Id(resourceId, lessonId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
		lessonResourceRepository.delete(resource);
	}

	private ClassEntity loadClassWithTeachers(Long classId) {
		return classRepository.findById(classId)
				.map(c -> {
					c.getTeachers().size(); // init teachers
					return c;
				})
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
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
		clazz.getTeachers().size(); // init teachers
		return clazz;
	}

	private Lesson ensureLessonExists(Long lessonId, Long classId) {
		return lessonRepository.findByIdAndClazz_Id(lessonId, classId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
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

	private LessonResponse toResponseWithResources(Lesson lesson) {
		List<LessonResourceResponse> resources = lessonResourceRepository.findByLesson_Id(lesson.getId())
				.stream()
				.map(this::toResourceResponse)
				.collect(Collectors.toList());

		Long classId = lesson.getClazz() != null ? lesson.getClazz().getId() : null;
		Long createdBy = lesson.getCreatedBy() != null ? lesson.getCreatedBy().getId() : null;

		return LessonResponse.builder()
				.id(lesson.getId())
				.title(lesson.getTitle())
				.description(trimToNull(lesson.getDescription()))
				.orderIndex(lesson.getOrderIndex())
				.classId(classId)
				.createdBy(createdBy)
				.createdAt(lesson.getCreatedAt())
				.updatedAt(lesson.getUpdatedAt())
				.resources(resources)
				.build();
	}

	private LessonResourceResponse toResourceResponse(LessonResource resource) {
		return LessonResourceResponse.builder()
				.id(resource.getId())
				.type(trimToNull(resource.getType()))
				.title(trimToNull(resource.getTitle()))
				.content(trimToNull(resource.getContent()))
				.url(trimToNull(resource.getUrl()))
				.filePath(trimToNull(resource.getFilePath()))
				.createdAt(resource.getCreatedAt())
				.updatedAt(resource.getUpdatedAt())
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
