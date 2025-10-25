package com.finalproject.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String emailAddress;
    private String city;
    private String country;
    private String timezone;
    private String phoneNumber;
}
