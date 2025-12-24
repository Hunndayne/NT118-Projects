package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.GradeRequest;
import com.finalproject.backend.dto.request.SubmissionRequest;
import com.finalproject.backend.dto.response.SubmissionResponse;
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

import java.time.Instant;
import java.time.OffsetDateTime;
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

    @Transactional
    public SubmissionResponse submitAssignment(String token, Long assignmentId, SubmissionRequest request) {
        User student = userService.getAuthenticatedUserEntity(token);
        Assignment assignment = loadAssignment(assignmentId);
        if (assignment.getDeadline() != null
                && OffsetDateTime.now().isAfter(assignment.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment deadline has passed");
        }
        ClassEntity clazz = assignment.getClazz();
        if (!canSubmit(student, clazz)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to submit for this assignment");
        }

        Submission submission = submissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, student.getId())
                .orElseGet(() -> Submission.builder()
                        .assignment(assignment)
                        .student(student)
                        .attemptNo(0)
                        .build());
        if (submission.getScore() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission already graded");
        }

        submission.setAttemptNo(submission.getAttemptNo() + 1);
        submission.setContent(trimToNull(request.getContent()));
        submission.setFileUrl(trimToNull(request.getFileUrl()));
        submission.setSubmittedAt(Instant.now());
        submission.setStatus("SUBMITTED");

        Submission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getMySubmission(String token, Long assignmentId) {
        User student = userService.getAuthenticatedUserEntity(token);
        Assignment assignment = loadAssignment(assignmentId);
        ClassEntity clazz = assignment.getClazz();
        if (!canSubmit(student, clazz)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view this submission");
        }
        Submission submission = submissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        return toResponse(submission);
    }

    @Transactional
    public void deleteMySubmission(String token, Long assignmentId) {
        User student = userService.getAuthenticatedUserEntity(token);
        Assignment assignment = loadAssignment(assignmentId);
        ClassEntity clazz = assignment.getClazz();
        if (!canSubmit(student, clazz)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to remove submission");
        }
        if (assignment.getDeadline() != null
                && OffsetDateTime.now().isAfter(assignment.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment deadline has passed");
        }
        Submission submission = submissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, student.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        if (submission.getScore() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission already graded");
        }
        submissionRepository.delete(submission);
    }

    @Transactional
    public SubmissionResponse gradeSubmission(String token, Long assignmentId, Long submissionId, GradeRequest request) {
        User grader = userService.getAuthenticatedUserEntity(token);
        Assignment assignment = loadAssignment(assignmentId);
        ClassEntity clazz = assignment.getClazz();
        boolean allowed = canGrade(grader, clazz);
        if (!allowed && grader.isTeacher()
                && assignment.getCreatedBy() != null
                && assignment.getCreatedBy().getId() != null
                && assignment.getCreatedBy().getId().equals(grader.getId())) {
            allowed = true;
        }
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to grade this assignment");
        }

        Submission submission = submissionRepository.findByIdAndAssignment_Id(submissionId, assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        if (request.getScore() != null) {
            submission.setScore(request.getScore());
            if (request.getStatus() == null) {
                submission.setStatus("GRADED");
            }
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

    private SubmissionStatusResponse toStatusResponse(User student, Submission submission, Assignment assignment) {
        boolean submitted = submission != null;
        return new SubmissionStatusResponse(
                submission != null ? submission.getId() : null,
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
