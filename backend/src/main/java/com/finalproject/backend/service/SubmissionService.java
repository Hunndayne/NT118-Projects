package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.GradeRequest;
import com.finalproject.backend.dto.request.SubmissionRequest;
import com.finalproject.backend.dto.response.SubmissionResponse;
import com.finalproject.backend.entity.Assignment;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.Submission;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.AssignmentRepository;
import com.finalproject.backend.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

	private final SubmissionRepository submissionRepository;
	private final AssignmentRepository assignmentRepository;
	private final UserService userService;

	@Transactional
	public SubmissionResponse submitAssignment(String token, Long assignmentId, SubmissionRequest request) {
		User student = userService.getAuthenticatedUserEntity(token);
		Assignment assignment = loadAssignment(assignmentId);
		ClassEntity clazz = assignment.getClazz();
		if (!canSubmit(student, clazz)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to submit for this assignment");
		}

		Submission submission = submissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, student.getId())
				.orElseGet(() -> Submission.builder()
						.assignment(assignment)
						.student(student)
						.attemptNo(1)
						.build());

		if (submission.getAttemptNo() != null) {
			submission.setAttemptNo(submission.getAttemptNo() + 1);
		} else {
			submission.setAttemptNo(1);
		}

		submission.setContent(trimToNull(request.getContent()));
		submission.setFileUrl(trimToNull(request.getFileUrl()));
		submission.setSubmittedAt(Instant.now());
		submission.setStatus("SUBMITTED");

		Submission saved = submissionRepository.save(submission);
		return toResponse(saved);
	}

	@Transactional
	public SubmissionResponse gradeSubmission(String token, Long assignmentId, Long submissionId, GradeRequest request) {
		User grader = userService.getAuthenticatedUserEntity(token);
		Assignment assignment = loadAssignment(assignmentId);
		ClassEntity clazz = assignment.getClazz();
		if (!canGrade(grader, clazz)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to grade this assignment");
		}

		Submission submission = submissionRepository.findByIdAndAssignment_Id(submissionId, assignmentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

		if (request.getScore() != null) {
			submission.setScore(request.getScore());
		}
		if (request.getFeedback() != null) {
			submission.setFeedback(trimToNull(request.getFeedback()));
		}
		if (request.getStatus() != null) {
			submission.setStatus(request.getStatus().trim());
		}
		submission.setGradedBy(grader);
		submission.setGradeAt(Instant.now());

		Submission saved = submissionRepository.save(submission);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public SubmissionResponse getSubmission(String token, Long assignmentId, Long submissionId) {
		User requester = userService.getAuthenticatedUserEntity(token);
		Assignment assignment = loadAssignment(assignmentId);
		ClassEntity clazz = assignment.getClazz();
		Submission submission = submissionRepository.findByIdAndAssignment_Id(submissionId, assignmentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

		if (requester.isSuperAdmin() || canGrade(requester, clazz)) {
			return toResponse(submission);
		}
		if (submission.getStudent() != null && submission.getStudent().getId().equals(requester.getId())) {
			return toResponse(submission);
		}
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to submission");
	}

	private boolean canSubmit(User student, ClassEntity clazz) {
		if (student.isSuperAdmin()) {
			return true;
		}
		if (!student.isStudent()) {
			return false;
		}
		Course course = clazz != null ? clazz.getCourse() : null;
		if (course == null || course.getId() == null) {
			return false;
		}
		return course.getStudents().stream().anyMatch(s -> s.getId().equals(student.getId()));
	}

	private boolean canGrade(User grader, ClassEntity clazz) {
		if (grader.isSuperAdmin()) {
			return true;
		}
		if (grader.isTeacher()) {
			return clazz.getTeachers().stream().anyMatch(t -> t.getId().equals(grader.getId()));
		}
		return false;
	}

	private Assignment loadAssignment(Long assignmentId) {
		return assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
	}

	private SubmissionResponse toResponse(Submission submission) {
		return SubmissionResponse.builder()
				.id(submission.getId())
				.assignmentId(submission.getAssignment() != null ? submission.getAssignment().getId() : null)
				.studentId(submission.getStudent() != null ? submission.getStudent().getId() : null)
				.attemptNo(submission.getAttemptNo())
				.submittedAt(submission.getSubmittedAt())
				.content(submission.getContent())
				.fileUrl(submission.getFileUrl())
				.status(submission.getStatus())
				.score(submission.getScore())
				.feedback(submission.getFeedback())
				.gradedBy(submission.getGradedBy() != null ? submission.getGradedBy().getId() : null)
				.gradeAt(submission.getGradeAt())
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
