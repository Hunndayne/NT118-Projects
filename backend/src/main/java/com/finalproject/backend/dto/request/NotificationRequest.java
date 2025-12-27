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
import lombok.Data;

@Data
public class NotificationRequest {
    
    @NotBlank(message = "Type must not be blank")
    private String type; // Event, Remind, Warning, Announcement
    
    @NotBlank(message = "Title must not be blank")
    private String title;
    
    @NotBlank(message = "Content must not be blank")
    private String content;
    
    private Long targetUserId; // null = broadcast to all
    private Long targetClassId; // null = no class restriction
}
