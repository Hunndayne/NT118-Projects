package com.finalproject.backend.service;

import com.finalproject.backend.dto.response.SubmissionStatusResponse;
import com.finalproject.backend.entity.Assignment;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.Submission;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.AssignmentRepository;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import com.finalproject.backend.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

	private final SubmissionRepository submissionRepository;
	private final AssignmentRepository assignmentRepository;
	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final UserService userService;

	@Transactional(readOnly = true)
	public List<SubmissionStatusResponse> getSubmissionStatus(String token, Long classId, Long assignmentId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		ClassEntity clazz = resolveClass(classId);
		requireTeacherOrAdmin(requester, clazz);

		Assignment assignment = assignmentRepository.findByIdAndClazz_Id(assignmentId, clazz.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

		List<Submission> submissions = submissionRepository.findByAssignment_Id(assignment.getId());
		Map<Long, Submission> submissionMap = new HashMap<>();
		for (Submission submission : submissions) {
			if (submission.getStudent() != null) {
				submissionMap.put(submission.getStudent().getId(), submission);
			}
		}

		Course course = clazz.getCourse();
		if (course == null) {
			return List.of();
		}
		course.getStudents().size();
		return course.getStudents().stream()
				.filter(student -> student.getRole() == null || student.getRole().isStudent())
				.map(student -> toStatusResponse(student, submissionMap.get(student.getId()), assignment))
				.collect(Collectors.toList());
	}

	private SubmissionStatusResponse toStatusResponse(User student, Submission submission, Assignment assignment) {
		boolean submitted = submission != null;
		return new SubmissionStatusResponse(
				student.getId(),
				student.getFirstName(),
				student.getLastName(),
				submitted,
				submission != null ? submission.getSubmittedAt() : null,
				submission != null ? submission.getScore() : null,
				submission != null ? submission.getStatus() : null,
				submission != null ? submission.getFileUrl() : null,
				assignment != null ? assignment.getDeadline() : null
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
}
