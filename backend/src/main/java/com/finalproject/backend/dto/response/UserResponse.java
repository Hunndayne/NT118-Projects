package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserResponse {
	Long id;
	String username;
	String email;
	String phone;
	String fullName;
	boolean active;
	boolean admin;
	Instant createdAt;
	Instant lastLoginAt;
}
