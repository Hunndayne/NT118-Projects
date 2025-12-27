package com.finalproject.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class NotificationResponse {

	private final Long id;
	private final String type;
	private final String title;
	private final String content;
	private final Instant createdAt;
	@JsonProperty("isRead")
	private final boolean read;
	private final Long userId;
	private final String senderName;

	public NotificationResponse(Long id,
	                            String type,
	                            String title,
	                            String content,
	                            Instant createdAt,
	                            boolean read,
	                            Long userId,
	                            String senderName) {
		this.id = id;
		this.type = type;
		this.title = title;
		this.content = content;
		this.createdAt = createdAt;
		this.read = read;
		this.userId = userId;
		this.senderName = senderName;
	}

	public Long getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isRead() {
		return read;
	}

	public Long getUserId() {
		return userId;
	}

	public String getSenderName() {
		return senderName;
	}
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String senderName;
    private Long userId;
    private boolean isRead;
    private String createdAt; // ISO format string for Android compatibility
}
