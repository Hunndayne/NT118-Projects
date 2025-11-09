package com.finalproject.backend.dto.response;

import java.time.Instant;

public class TokenStatusResponse {

	private final boolean active;
	private final Instant expiresAt;

	public TokenStatusResponse(boolean active, Instant expiresAt) {
		this.active = active;
		this.expiresAt = expiresAt;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
