package com.example.enggo.admin;

public class UserAdmin {

    private long id;

    // ===== BASIC INFO =====
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;

    private String emailAddress;
    private String emailVisibility;
    private String city;
    private String country;
    private String timezone;
    private String description;
    private String interest;
    private String phoneNumber;

    private boolean admin;
    private boolean active;
    private String role;

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

    public String getEmailVisibility() {
        return emailVisibility;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getDescription() {
        return description;
    }

    public String getInterest() {
        return interest;
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

    public String getRole() {
        return role;
    }

    // ===== HELPER =====
    public String getStatusText() {
        return active ? "Active" : "Locked";
    }

    public void setActive(boolean b) {
        this.active = b;
    }
}
