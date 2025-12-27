package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.NotificationRequest;
import com.finalproject.backend.dto.response.NotificationResponse;
import com.finalproject.backend.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
=======
import org.springframework.web.bind.annotation.*;

import java.util.List;
>>>>>>> 15646fcd7e4282cf39290213b2b470e2c7dd21be

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

<<<<<<< HEAD
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@RequestHeader("X-Auth-Token") String token,
                           @PathVariable("id") Long notificationId) {
        notificationService.markAsRead(token, notificationId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@RequestHeader("X-Auth-Token") String token,
                                   @PathVariable("id") Long notificationId) {
        notificationService.deleteNotification(token, notificationId);
    }
=======
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
>>>>>>> 15646fcd7e4282cf39290213b2b470e2c7dd21be
}
