package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class UserResponse {
    Long id;
    String username;
    String firstName;
    String lastName;
    String fullName;
    String emailAddress;
    String emailVisibility;
    String city;
    String country;
    String timezone;
    String description;
    String interest;
    String phoneNumber;
    String avatarUrl;
    boolean active;
    boolean admin;
    Instant createdAt;
    Instant lastLoginAt;
}
