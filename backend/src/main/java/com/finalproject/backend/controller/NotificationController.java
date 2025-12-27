package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.NotificationRequest;
import com.finalproject.backend.dto.response.NotificationResponse;
import com.finalproject.backend.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public List<NotificationResponse> getNotifications(@RequestHeader("X-Auth-Token") String token) {
		return notificationService.getNotifications(token);
	}

	@GetMapping("/{id}")
	public NotificationResponse getNotification(@RequestHeader("X-Auth-Token") String token,
	                                            @PathVariable("id") Long notificationId) {
		return notificationService.getNotification(token, notificationId);
	}

	@PostMapping
	public NotificationResponse sendNotification(@RequestHeader("X-Auth-Token") String token,
	                                             @Valid @RequestBody NotificationRequest request) {
		return notificationService.createNotification(token, request);
	}

	@PutMapping("/{id}/read")
	public void markAsRead(@RequestHeader("X-Auth-Token") String token,
	                       @PathVariable("id") Long notificationId) {
		notificationService.markAsRead(token, notificationId);
	}

	@DeleteMapping("/{id}")
	public void deleteNotification(@RequestHeader("X-Auth-Token") String token,
	                               @PathVariable("id") Long notificationId) {
		notificationService.deleteNotification(token, notificationId);
	}
    private final NotificationService notificationService;

    /**
     * Get all notifications for current user
     */
    @GetMapping
    public List<NotificationResponse> getNotifications(@RequestHeader("X-Auth-Token") String token) {
        return notificationService.getNotificationsForUser(token);
    }

    /**
     * Get single notification by ID
     */
    @GetMapping("/{id}")
    public NotificationResponse getNotification(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable("id") Long notificationId
    ) {
        return notificationService.getNotificationById(token, notificationId);
    }

    /**
     * Send notification (Teacher/Admin only)
     */
    @PostMapping
    public NotificationResponse sendNotification(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody NotificationRequest request
    ) {
        return notificationService.sendNotification(token, request);
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable("id") Long notificationId
    ) {
        notificationService.markAsRead(token, notificationId);
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable("id") Long notificationId
    ) {
        notificationService.deleteNotification(token, notificationId);
    }
}
