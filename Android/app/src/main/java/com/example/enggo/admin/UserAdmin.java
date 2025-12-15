package com.example.enggo.admin;

public class UserAdmin {

    private long id;
    private String fullName;
    private String emailAddress;
    private boolean active;

    // ===== GETTERS =====
    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isActive() {
        return active;
    }

    // ===== HELPER =====
    public String getStatusText() {
        return active ? "Active" : "Locked";
    }
}