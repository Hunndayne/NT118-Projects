package com.finalproject.backend.service;

import com.finalproject.backend.config.R2Properties;
import com.finalproject.backend.dto.request.PresignUploadRequest;
import com.finalproject.backend.dto.response.PresignUploadResponse;
import com.finalproject.backend.entity.Assignment;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.entity.UserRole;
import com.finalproject.backend.repository.AssignmentRepository;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import com.finalproject.backend.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

	private final R2Properties r2Properties;
	private final ObjectProvider<S3Presigner> presignerProvider;
	private final UserService userService;
	private final ClassRepository classRepository;
	private final LessonRepository lessonRepository;
	private final AssignmentRepository assignmentRepository;
	private final CourseRepository courseRepository;

	@Transactional(readOnly = true)
	public PresignUploadResponse presignUpload(String token, PresignUploadRequest request) {
		S3Presigner presigner = presignerProvider.getIfAvailable();
		if (presigner == null || !r2Properties.isConfigured()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "R2 is not configured");
		}

		User user = userService.getAuthenticatedUserEntity(token);
		UserRole role = user.getRole() != null ? user.getRole() : UserRole.STUDENT;

		UploadPurpose purpose = parsePurpose(request.getPurpose());
		String contentType = trimRequired(request.getContentType(), "contentType");
		String safeFileName = sanitizeFileName(request.getFileName());

		String key = buildKey(purpose, user, role, request, safeFileName);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(r2Properties.getBucket().trim())
				.key(key)
				.contentType(contentType)
				.build();

		PresignedPutObjectRequest presigned = presigner.presignPutObject(PutObjectPresignRequest.builder()
				.signatureDuration(r2Properties.getPresignDuration())
				.putObjectRequest(putObjectRequest)
				.build());

		URL url = presigned.url();
		String publicBaseUrl = r2Properties.resolvePublicBaseUrl();
		if (publicBaseUrl == null) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "R2 publicBaseUrl is not configured");
		}

		String publicUrl = publicBaseUrl + "/" + key;
		Instant expiresAt = Instant.now().plus(r2Properties.getPresignDuration());

		return PresignUploadResponse.builder()
				.key(key)
				.uploadUrl(url.toString())
				.publicUrl(publicUrl)
				.expiresAt(expiresAt)
				.contentType(contentType)
				.build();
	}

	private String buildKey(UploadPurpose purpose,
	                        User user,
	                        UserRole role,
	                        PresignUploadRequest request,
	                        String safeFileName) {
		String randomPart = UUID.randomUUID().toString();

		return switch (purpose) {
			case AVATAR -> "avatars/" + user.getId() + "/" + randomPart + "-" + safeFileName;
			case LESSON_RESOURCE -> {
				Long classIdOrCourseId = request.getClassId();
				Long lessonId = request.getLessonId();
				if (classIdOrCourseId == null || lessonId == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "classId and lessonId are required");
				}

				ClassEntity clazz = resolveClass(classIdOrCourseId);
				Long resolvedClassId = clazz.getId();

				if (!role.isSuperAdmin()) {
					boolean isTeacherOfClass = role.isTeacher()
							&& classRepository.existsByIdAndTeachers_Id(resolvedClassId, user.getId());
					if (!isTeacherOfClass) {
						throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to upload resources for this class");
					}
				}

				boolean lessonBelongsToClass = lessonRepository.findByIdAndClazz_Id(lessonId, resolvedClassId).isPresent();
				if (!lessonBelongsToClass) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lessonId does not belong to classId");
				}

				yield "lesson-resources/" + resolvedClassId + "/" + lessonId + "/" + randomPart + "-" + safeFileName;
			}
			case SUBMISSION -> {
				Long assignmentId = request.getAssignmentId();
				if (assignmentId == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignmentId is required");
				}

				if (!role.isSuperAdmin() && !role.isStudent()) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only student can upload submission file");
				}

				Assignment assignment = assignmentRepository.findById(assignmentId)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

				ClassEntity clazz = assignment.getClazz();
				Long courseId = clazz != null && clazz.getCourse() != null ? clazz.getCourse().getId() : null;
				if (courseId == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment is not linked to a course");
				}

				if (!role.isSuperAdmin()) {
					boolean enrolled = courseRepository.findByIdAndStudents_Id(courseId, user.getId()).isPresent();
					if (!enrolled) {
						throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enrolled in this course");
					}
				}

				yield "submissions/" + assignmentId + "/" + user.getId() + "/" + randomPart + "-" + safeFileName;
			}
		};
	}

	private ClassEntity resolveClass(Long classIdOrCourseId) {
		ClassEntity clazz = classRepository.findById(classIdOrCourseId)
				.orElseGet(() -> classRepository.findFirstByCourse_Id(classIdOrCourseId));
		if (clazz == null) {
			Course course = courseRepository.findById(classIdOrCourseId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
			if (course.getTeachers() != null) {
				course.getTeachers().size();
			}
			if (course.getStudents() != null) {
				course.getStudents().size();
			}
			clazz = ClassEntity.builder()
					.name(course.getName())
					.description(course.getDescription())
					.active(course.getActive())
					.course(course)
					.createdBy(course.getCreatedBy())
					.teachers(course.getTeachers() != null
							? new java.util.HashSet<>(course.getTeachers())
							: new java.util.HashSet<>())
					.students(course.getStudents() != null
							? new java.util.HashSet<>(course.getStudents())
							: new java.util.HashSet<>())
					.build();
			clazz = classRepository.save(clazz);
		}
		if (clazz.getTeachers() != null) {
			clazz.getTeachers().size();
		}
		return clazz;
	}

	private UploadPurpose parsePurpose(String value) {
		if (value == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purpose is required");
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT);
		try {
			return UploadPurpose.valueOf(normalized);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid purpose");
		}
	}

	private String sanitizeFileName(String fileName) {
		String value = trimRequired(fileName, "fileName");
		String basename = value.replace("\\", "/");
		if (basename.contains("/")) {
			basename = basename.substring(basename.lastIndexOf('/') + 1);
		}
		basename = basename.replaceAll("[^A-Za-z0-9._-]", "_");
		if (basename.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fileName");
		}
		if (basename.length() > 120) {
			basename = basename.substring(basename.length() - 120);
		}
		return basename;
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

	private enum UploadPurpose {
		AVATAR,
		LESSON_RESOURCE,
		SUBMISSION
	}
}
