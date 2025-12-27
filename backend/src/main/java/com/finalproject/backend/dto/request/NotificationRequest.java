package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {

	@NotBlank(message = "type must not be blank")
	private String type;

	@NotBlank(message = "title must not be blank")
	private String title;

	private String content;

	private Long targetUserId;

	private Long targetClassId;

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public Long getTargetUserId() {
		return targetUserId;
	}

	public Long getTargetClassId() {
		return targetClassId;
	}
}
