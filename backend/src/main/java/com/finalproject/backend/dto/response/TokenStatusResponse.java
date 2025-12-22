package com.finalproject.backend.dto.response;

import java.time.Instant;

public class TokenStatusResponse {

    private boolean loggedIn;
    private boolean admin;
    private Instant expiresAt;
    private String role;

    public TokenStatusResponse(boolean loggedIn, boolean admin, Instant expiresAt, String role) {
        this.loggedIn = loggedIn;
        this.admin = admin;
        this.expiresAt = expiresAt;
        this.role = role;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getRole() {
        return role;
    }
}
