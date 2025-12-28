package com.example.enggo.admin;

import com.google.gson.annotations.SerializedName;

public class UserAdmin {

    private long id;

    // ===== BASIC INFO =====
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;

    private String emailAddress; // Tên biến khớp với JSON "emailAddress"
    private String emailVisibility;
    private String city;
    private String country;
    private String timezone;
    private String description;
    private String interest;
    private String phoneNumber;
    private String avatarUrl;

    private boolean admin;
    private boolean active;
    private String role;

    // ===== NEW FIELD =====
    // Thêm trường này để hứng dữ liệu "lastLoginAt" từ JSON
    @SerializedName("lastLoginAt")
    private String lastLoginAt;

    // ===== GETTERS (GIỮ NGUYÊN) =====
    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return fullName; }
    public String getEmailAddress() { return emailAddress; } // Project dùng getEmailAddress()
    public String getEmailVisibility() { return emailVisibility; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getTimezone() { return timezone; }
    public String getDescription() { return description; }
    public String getInterest() { return interest; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isAdmin() { return admin; }
    public boolean isActive() { return active; }
    public String getRole() { return role; }

    // ===== NEW GETTER =====
    public String getLastLoginAt() { return lastLoginAt; }

    // ===== HELPER (GIỮ NGUYÊN) =====
    public String getStatusText() {
        return active ? "Active" : "Locked";
    }

    public void setActive(boolean b) {
        this.active = b;
    }
}