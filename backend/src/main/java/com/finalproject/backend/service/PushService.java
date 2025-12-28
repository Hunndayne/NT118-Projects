package com.finalproject.backend.service;

import com.finalproject.backend.config.FcmProperties;
import com.finalproject.backend.entity.ClassEntity;
import com.finalproject.backend.entity.Course;
import com.finalproject.backend.entity.DeviceToken;
import com.finalproject.backend.entity.Notification;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.ClassRepository;
import com.finalproject.backend.repository.CourseRepository;
import com.finalproject.backend.repository.DeviceTokenRepository;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PushService {

	private static final Logger log = LoggerFactory.getLogger(PushService.class);
	private static final int FCM_BATCH_SIZE = 500;

	private final DeviceTokenRepository deviceTokenRepository;
	private final ClassRepository classRepository;
	private final CourseRepository courseRepository;
	private final FcmProperties fcmProperties;

	private FirebaseApp firebaseApp;

	public PushService(DeviceTokenRepository deviceTokenRepository,
	                   ClassRepository classRepository,
	                   CourseRepository courseRepository,
	                   FcmProperties fcmProperties) {
		this.deviceTokenRepository = deviceTokenRepository;
		this.classRepository = classRepository;
		this.courseRepository = courseRepository;
		this.fcmProperties = fcmProperties;
	}

	public void dispatchNotification(Notification notification) {
		if (notification == null) {
			return;
		}
		boolean hasExplicitTarget = notification.getTargetUser() != null || notification.getTargetClass() != null;
		List<DeviceToken> tokens;
		if (hasExplicitTarget) {
			Set<Long> targetUserIds = resolveTargetUserIds(notification);
			if (targetUserIds.isEmpty()) {
				return;
			}
			tokens = deviceTokenRepository.findAllByUser_IdInAndActiveTrue(targetUserIds);
		} else {
			tokens = deviceTokenRepository.findAllByActiveTrue();
		}
		if (tokens.isEmpty()) {
			return;
		}
		Map<String, String> data = buildDataPayload(notification);
		sendToTokens(tokens, notification.getTitle(), notification.getContent(), data);
	}

	private Set<Long> resolveTargetUserIds(Notification notification) {
		User targetUser = notification.getTargetUser();
		if (targetUser != null && targetUser.getId() != null) {
			return Collections.singleton(targetUser.getId());
		}
		ClassEntity targetClass = notification.getTargetClass();
		if (targetClass == null || targetClass.getId() == null) {
			return Collections.emptySet();
		}
		ClassEntity hydratedClass = classRepository.findById(targetClass.getId()).orElse(targetClass);
		Course course = hydratedClass.getCourse();
		if (course == null || course.getId() == null) {
			return Collections.emptySet();
		}
		Course hydratedCourse = courseRepository.findById(course.getId()).orElse(course);
		hydratedCourse.getStudents().size();
		return hydratedCourse.getStudents().stream()
				.map(User::getId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Map<String, String> buildDataPayload(Notification notification) {
		Map<String, String> data = new HashMap<>();
		if (notification.getId() != null) {
			data.put("notificationId", String.valueOf(notification.getId()));
		}
		if (notification.getTargetClass() != null && notification.getTargetClass().getId() != null) {
			data.put("classId", String.valueOf(notification.getTargetClass().getId()));
		}
		if (notification.getCreatedBy() != null && notification.getCreatedBy().getId() != null) {
			data.put("createdBy", String.valueOf(notification.getCreatedBy().getId()));
		}
		return data;
	}

	private void sendToTokens(List<DeviceToken> tokens, String title, String body, Map<String, String> data) {
		FirebaseApp app = getFirebaseApp();
		if (app == null) {
			log.debug("FCM not configured; skip push.");
			return;
		}
		List<String> tokenValues = tokens.stream()
				.map(DeviceToken::getToken)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
		if (tokenValues.isEmpty()) {
			return;
		}
		String safeTitle = title == null ? "" : title;
		String safeBody = body == null ? "" : body;

		for (int i = 0; i < tokenValues.size(); i += FCM_BATCH_SIZE) {
			int end = Math.min(i + FCM_BATCH_SIZE, tokenValues.size());
			List<String> batch = tokenValues.subList(i, end);
			MulticastMessage message = MulticastMessage.builder()
					.addAllTokens(batch)
					.setNotification(com.google.firebase.messaging.Notification.builder()
							.setTitle(safeTitle)
							.setBody(safeBody)
							.build())
					.putAllData(data)
					.build();
			try {
				BatchResponse response = FirebaseMessaging.getInstance(app).sendEachForMulticast(message);
				handlePushErrors(batch, response);
			} catch (FirebaseMessagingException e) {
				log.warn("Failed to send FCM multicast: {}", e.getMessage());
			}
		}
	}

	private void handlePushErrors(List<String> batch, BatchResponse response) {
		if (response == null) {
			return;
		}
		for (int i = 0; i < response.getResponses().size(); i++) {
			if (response.getResponses().get(i).isSuccessful()) {
				continue;
			}
			Exception exception = response.getResponses().get(i).getException();
			if (exception instanceof FirebaseMessagingException fme) {
				if (fme.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
					String token = batch.get(i);
					deviceTokenRepository.findByToken(token).ifPresent(stored -> {
						stored.setActive(false);
						deviceTokenRepository.save(stored);
					});
				}
			}
		}
	}

	private synchronized FirebaseApp getFirebaseApp() {
		if (firebaseApp != null) {
			return firebaseApp;
		}
		String rawJson = trimToNull(fcmProperties.getServiceAccountJson());
		String path = trimToNull(fcmProperties.getServiceAccountPath());
		if (rawJson == null && path == null) {
			return null;
		}
		try (InputStream inputStream = rawJson != null
				? new ByteArrayInputStream(rawJson.getBytes(StandardCharsets.UTF_8))
				: new FileInputStream(path)) {
			ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(inputStream);
			String projectId = trimToNull(fcmProperties.getProjectId());
			if (projectId == null) {
				projectId = credentials.getProjectId();
			}
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(credentials)
					.setProjectId(projectId)
					.build();
			firebaseApp = FirebaseApp.initializeApp(options, "nt118");
			return firebaseApp;
		} catch (Exception e) {
			log.warn("Failed to initialize FirebaseApp: {}", e.getMessage());
			return null;
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
