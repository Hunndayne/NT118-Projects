package com.finalproject.backend.service;

import com.finalproject.backend.dto.response.CalendarEventResponse;
import com.finalproject.backend.entity.Assignment;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.entity.UserRole;
import com.finalproject.backend.repository.AssignmentRepository;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

	private final AssignmentRepository assignmentRepository;
	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final UserService userService;

	@Transactional(readOnly = true)
	public List<CalendarEventResponse> getEventsForCurrentUser(String token) {
		User user = userService.getAuthenticatedUserEntity(token);
		UserRole role = effectiveRole(user);
		List<Assignment> assignments;

		if (role.isSuperAdmin()) {
			assignments = assignmentRepository.findAll();
		} else if (role.isTeacher()) {
			assignments = assignmentRepository.findDistinctByClazz_Teachers_Id(user.getId());
		} else if (role.isStudent()) {
			assignments = assignmentRepository.findDistinctByClazz_Course_Students_Id(user.getId());
		} else {
			assignments = new ArrayList<>();
		}

		return assignments.stream()
				.map(this::toEventResponse)
				.collect(Collectors.toList());
	}

	private CalendarEventResponse toEventResponse(Assignment assignment) {
		ClassEntity clazz = assignment.getClazz();
		Course course = clazz != null ? clazz.getCourse() : null;
		return CalendarEventResponse.builder()
				.id(assignment.getId())
				.title(assignment.getTitle())
				.description(assignment.getDescription())
				.deadline(assignment.getDeadline())
				.classId(clazz != null ? clazz.getId() : null)
				.className(clazz != null ? clazz.getName() : null)
				.courseId(course != null ? course.getId() : null)
				.courseName(course != null ? course.getName() : null)
				.weight(assignment.getWeight())
				.createdBy(assignment.getCreatedBy() != null ? assignment.getCreatedBy().getId() : null)
				.createdAt(assignment.getCreatedAt())
				.build();
	}

	private UserRole effectiveRole(User user) {
		UserRole role = user.getRole();
		if (role != null) {
			return role;
		}
		return user.isAdmin() ? UserRole.SUPER_ADMIN : UserRole.STUDENT;
	}
}
