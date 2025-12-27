package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.NotificationRequest;
import com.finalproject.backend.dto.response.NotificationResponse;
import com.finalproject.backend.entity.Notification;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Transactional
    public NotificationResponse sendNotification(String token, NotificationRequest request) {
        User sender = userService.getAuthenticatedUserEntity(token);
        
        // Only teachers and admins can send notifications
        if (!sender.isTeacher() && !sender.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers and admins can send notifications");
        }

        User targetUser = null;
        if (request.getTargetUserId() != null) {
            targetUser = userService.loadUserEntity(request.getTargetUserId());
        }

        Notification notification = Notification.builder()
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .sender(sender)
                .targetUser(targetUser)
                .targetClassId(request.getTargetClassId())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(String token) {
        User user = userService.getAuthenticatedUserEntity(token);
        List<Notification> notifications = notificationRepository.findNotificationsForUser(user.getId());
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(String token, Long notificationId) {
        User user = userService.getAuthenticatedUserEntity(token);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        
        // Check permission: user can only view their own notifications or broadcast ones
        if (notification.getTargetUser() != null && !notification.getTargetUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access this notification");
        }
        
        return toResponse(notification);
    }

    @Transactional
    public void markAsRead(String token, Long notificationId) {
        User user = userService.getAuthenticatedUserEntity(token);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        
        // Check permission
        if (notification.getTargetUser() != null && !notification.getTargetUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify this notification");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(String token, Long notificationId) {
        User user = userService.getAuthenticatedUserEntity(token);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        
        // Only sender or admin can delete
        if (!notification.getSender().getId().equals(user.getId()) && !user.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this notification");
        }
        
        notificationRepository.delete(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        String senderName = notification.getSender() != null ? 
                notification.getSender().getFullName() : "System";
        
        Long userId = notification.getTargetUser() != null ? 
                notification.getTargetUser().getId() : null;
        
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .senderName(senderName)
                .userId(userId)
                .isRead(notification.isRead())
                .createdAt(FORMATTER.format(notification.getCreatedAt()))
                .build();
    }
}
