package com.example.enggo.admin;

public class UserAdmin {
    private String name;
    private String email;
    private String status;
    // Bạn có thể thêm các trường khác như ID, v.v.

    public UserAdmin(String name, String email, String status) {
        this.name = name;
        this.email = email;
        this.status = status;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}