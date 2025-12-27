package com.finalproject.backend.dto.response;

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
