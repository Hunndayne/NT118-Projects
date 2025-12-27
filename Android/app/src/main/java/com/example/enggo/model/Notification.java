package com.example.enggo.model;

public class Notification {
    private Long id;
    private String type; // "Event", "Remind", "Warning", "Announcement"
    private String title;
    private String content;
    private String createdAt;
    private boolean isRead;
    private Long userId;
    private String senderName;

    // Constructor
    public Notification() {
    }

    public Notification(Long id, String type, String title, String content, String createdAt, boolean isRead) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // Helper method để lấy preview content (50 ký tự đầu)
    public String getPreviewContent() {
        if (content == null) return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
