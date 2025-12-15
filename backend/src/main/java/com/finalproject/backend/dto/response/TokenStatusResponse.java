package com.finalproject.backend.dto.response;

import java.time.Instant;

public class TokenStatusResponse {

    private boolean loggedIn;
    private boolean admin;
    private Instant expiresAt;

    public TokenStatusResponse(boolean loggedIn, boolean admin, Instant expiresAt) {
        this.loggedIn = loggedIn;
        this.admin = admin;
        this.expiresAt = expiresAt;
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
}
