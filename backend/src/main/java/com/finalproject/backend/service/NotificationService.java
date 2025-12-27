package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.NotificationRequest;
import com.finalproject.backend.dto.response.NotificationResponse;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.Notification;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import com.finalproject.backend.repository.NotificationRepository;
import com.finalproject.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final UserService userService;

	@Transactional(readOnly = true)
	public List<NotificationResponse> getNotifications(String rawToken) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		return notificationRepository.findAllByOrderByCreatedAtDesc()
				.stream()
				.filter(notification -> isVisibleTo(notification, requester))
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public NotificationResponse getNotification(String rawToken, Long notificationId) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		Notification notification = loadNotification(notificationId);
		if (!isVisibleTo(notification, requester)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}
		return toResponse(notification);
	}

	@Transactional
	public NotificationResponse createNotification(String rawToken, NotificationRequest request) {
		User sender = userService.getAuthenticatedUserEntity(rawToken);
		if (!sender.isSuperAdmin() && !sender.isTeacher()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin or teacher can send notifications");
		}

		String type = normalizeType(request.getType());
		String title = trimRequired(request.getTitle(), "title");
		String content = trimToNull(request.getContent());

		User targetUser = null;
		if (request.getTargetUserId() != null) {
			targetUser = userRepository.findById(request.getTargetUserId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));
		}

		ClassEntity targetClass = null;
		if (request.getTargetClassId() != null) {
			targetClass = classRepository.findById(request.getTargetClassId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target class not found"));
		}

		Notification notification = Notification.builder()
				.type(type)
				.title(title)
				.content(content)
				.createdBy(sender)
				.targetUser(targetUser)
				.targetClass(targetClass)
				.read(false)
				.build();

		Notification saved = notificationRepository.save(notification);
		return toResponse(saved);
	}

	@Transactional
	public void markAsRead(String rawToken, Long notificationId) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		Notification notification = loadNotification(notificationId);
		if (!isVisibleTo(notification, requester)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}
		notification.setRead(true);
		notificationRepository.save(notification);
	}

	@Transactional
	public void deleteNotification(String rawToken, Long notificationId) {
		User requester = userService.getAuthenticatedUserEntity(rawToken);
		Notification notification = loadNotification(notificationId);
		if (!requester.isSuperAdmin()
				&& notification.getCreatedBy() != null
				&& !notification.getCreatedBy().getId().equals(requester.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to delete this notification");
		}
		notificationRepository.delete(notification);
	}

	private boolean isVisibleTo(Notification notification, User requester) {
		if (requester.isSuperAdmin() || requester.isTeacher()) {
			return true;
		}

		User targetUser = notification.getTargetUser();
		if (targetUser != null) {
			return targetUser.getId().equals(requester.getId());
		}

		ClassEntity targetClass = notification.getTargetClass();
		if (targetClass == null) {
			return true;
		}

		return requester.isStudent() && isStudentInCourse(requester, targetClass);
	}

	private boolean isStudentInCourse(User student, ClassEntity clazz) {
		Course course = clazz != null ? clazz.getCourse() : null;
		if (course == null || course.getId() == null) {
			return false;
		}
		return courseRepository.findByIdAndStudents_Id(course.getId(), student.getId()).isPresent();
	}

	private NotificationResponse toResponse(Notification notification) {
		Long userId = notification.getTargetUser() != null ? notification.getTargetUser().getId() : null;
		String senderName = notification.getCreatedBy() != null ? notification.getCreatedBy().getFullName() : null;
		return new NotificationResponse(
				notification.getId(),
				notification.getType(),
				notification.getTitle(),
				notification.getContent(),
				notification.getCreatedAt(),
				notification.isRead(),
				userId,
				senderName
		);
	}

	private Notification loadNotification(Long id) {
		return notificationRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
	}

	private String normalizeType(String type) {
		if (type == null) {
			return "Announcement";
		}
		String trimmed = type.trim().toLowerCase(Locale.ROOT);
		switch (trimmed) {
			case "event":
				return "Event";
			case "remind":
				return "Remind";
			case "warning":
				return "Warning";
			case "announcement":
				return "Announcement";
			default:
				return "Announcement";
		}
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
