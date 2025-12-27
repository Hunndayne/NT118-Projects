package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
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
