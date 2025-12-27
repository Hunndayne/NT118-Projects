package com.example.enggo.model;

public class NotificationRequest {
    private String type;
    private String title;
    private String content;
    private Long targetUserId; // null = gửi cho tất cả
    private Long targetClassId; // null = không giới hạn lớp

    public NotificationRequest() {
    }

    public NotificationRequest(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Long getTargetClassId() {
        return targetClassId;
    }

    public void setTargetClassId(Long targetClassId) {
        this.targetClassId = targetClassId;
    }
}
