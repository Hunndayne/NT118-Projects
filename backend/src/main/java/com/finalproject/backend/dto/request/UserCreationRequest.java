package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
    @NotBlank(message = "username must not be blank")
    private String username;
    @NotBlank(message = "password must not be blank")
    private String password;
    @NotBlank(message = "firstname must not be blank")
    private String firstname;
    @NotBlank(message = "lastname must not be blank")
    private String lastname;
    @NotBlank(message = "emailAddress must not be blank")
    @Email
    private String emailAddress;
    private String city;
    private String country;
    private String timezone;
    @NotBlank(message = "phoneNumber must not be blank")
    private String phoneNumber;
}
