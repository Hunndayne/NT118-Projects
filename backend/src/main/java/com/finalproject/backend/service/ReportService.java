package com.finalproject.backend.service;

import com.finalproject.backend.dto.response.ReportOverviewResponse;
import com.finalproject.backend.dto.response.SubmissionStatisticsResponse;
import com.finalproject.backend.entity.Assignment;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.Submission;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.AssignmentRepository;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import com.finalproject.backend.repository.LessonRepository;
import com.finalproject.backend.repository.SubmissionRepository;
import com.finalproject.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public ReportOverviewResponse getOverview(String token) {
        User user = userService.getAuthenticatedUserEntity(token);
        if (!user.isSuperAdmin() && !user.isTeacher()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher or super admin required");
        }

        if (user.isSuperAdmin()) {
            int totalStudents = safeLongToInt(userRepository.countActiveStudents());
            int totalCourses = safeLongToInt(courseRepository.count());
            int totalClasses = safeLongToInt(classRepository.count());
            int totalModules = safeLongToInt(lessonRepository.count());
            SubmissionStatisticsResponse stats = buildSubmissionStatistics(assignmentRepository.findAll());
            return ReportOverviewResponse.builder()
                    .totalStudents(totalStudents)
                    .totalCourses(totalCourses)
                    .totalClasses(totalClasses)
                    .totalModules(totalModules)
                    .submissionStatistics(stats)
                    .build();
        }

        List<ClassEntity> classes = classRepository.findDistinctByTeachers_Id(user.getId());
        Set<Long> classIds = classes.stream()
                .map(ClassEntity::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> courseIds = new HashSet<>();
        for (ClassEntity clazz : classes) {
            Course course = clazz.getCourse();
            if (course != null && course.getId() != null) {
                courseIds.add(course.getId());
            }
        }

        int totalCourses = courseIds.size();
        int totalClasses = classes.size();
        int totalModules = classIds.isEmpty()
                ? 0
                : safeLongToInt(lessonRepository.countByClazz_IdIn(classIds));
        int totalStudents = courseIds.isEmpty()
                ? 0
                : safeLongToInt(courseRepository.countDistinctStudentsByCourseIds(courseIds));

        List<Assignment> assignments = classIds.isEmpty()
                ? List.of()
                : assignmentRepository.findByClazz_IdIn(classIds);

        SubmissionStatisticsResponse stats = buildSubmissionStatistics(assignments);

        return ReportOverviewResponse.builder()
                .totalStudents(totalStudents)
                .totalCourses(totalCourses)
                .totalClasses(totalClasses)
                .totalModules(totalModules)
                .submissionStatistics(stats)
                .build();
    }

    private SubmissionStatisticsResponse buildSubmissionStatistics(List<Assignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return SubmissionStatisticsResponse.builder()
                    .onTime(0)
                    .late(0)
                    .missing(0)
                    .submissionRate(0.0)
                    .build();
        }

        List<Long> assignmentIds = assignments.stream()
                .map(Assignment::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        Map<Long, Map<Long, Submission>> submissionMap = new HashMap<>();
        if (!assignmentIds.isEmpty()) {
            List<Submission> submissions = submissionRepository.findByAssignment_IdIn(assignmentIds);
            for (Submission submission : submissions) {
                Long assignmentId = submission.getAssignment() != null ? submission.getAssignment().getId() : null;
                Long studentId = submission.getStudent() != null ? submission.getStudent().getId() : null;
                if (assignmentId == null || studentId == null) {
                    continue;
                }
                submissionMap
                        .computeIfAbsent(assignmentId, key -> new HashMap<>())
                        .put(studentId, submission);
            }
        }

        Instant now = Instant.now();
        int onTime = 0;
        int late = 0;
        int missing = 0;
        int expected = 0;

        for (Assignment assignment : assignments) {
            OffsetDateTime deadline = assignment.getDeadline();
            if (deadline == null) {
                continue;
            }
            Instant deadlineInstant = deadline.toInstant();
            if (deadlineInstant.isAfter(now)) {
                continue;
            }
            Course course = assignment.getClazz() != null ? assignment.getClazz().getCourse() : null;
            if (course == null || course.getStudents() == null || course.getStudents().isEmpty()) {
                continue;
            }
            Map<Long, Submission> submissionsByStudent = submissionMap.getOrDefault(assignment.getId(), Map.of());
            expected += course.getStudents().size();
            for (User student : course.getStudents()) {
                if (student.getId() == null) {
                    continue;
                }
                Submission submission = submissionsByStudent.get(student.getId());
                if (submission != null && submission.getSubmittedAt() != null) {
                    if (submission.getSubmittedAt().isAfter(deadlineInstant)) {
                        late++;
                    } else {
                        onTime++;
                    }
                } else {
                    missing++;
                }
            }
        }

        double rate = expected == 0 ? 0.0 : ((double) (onTime + late) / (double) expected) * 100.0;
        rate = BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return SubmissionStatisticsResponse.builder()
                .onTime(onTime)
                .late(late)
                .missing(missing)
                .submissionRate(rate)
                .build();
    }

    private int safeLongToInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
