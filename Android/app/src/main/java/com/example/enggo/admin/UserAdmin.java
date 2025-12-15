package com.example.enggo.admin;

public class UserAdmin {

    private long id;

    // ===== BASIC INFO =====
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;

    private String emailAddress;
    private String phoneNumber;

    private boolean admin;
    private boolean active;

    // ===== GETTERS =====
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isActive() {
        return active;
    }

    // ===== HELPER =====
    public String getStatusText() {
        return active ? "Active" : "Locked";
    }
}
