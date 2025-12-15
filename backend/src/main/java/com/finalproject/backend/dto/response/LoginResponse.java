package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class LoginResponse {
	String token;
	String tokenType;
	Instant expiresAt;
	boolean admin;
	String role;
}
