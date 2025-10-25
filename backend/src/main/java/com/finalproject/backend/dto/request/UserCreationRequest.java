package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserCreationRequest {
	@NotBlank(message = "username must not be blank")
	@Pattern(regexp = "^[A-Za-z0-9_.]{3,32}$", message = "username must be 3-32 characters using letters, digits, underscore or dot")
	private String username;

	@NotBlank(message = "password must not be blank")
	private String password;

	@NotBlank(message = "email must not be blank")
	@Email
	private String email;

	@NotBlank(message = "fullName must not be blank")
	private String fullName;

	@Pattern(regexp = "^$|^\\+?[1-9]\\d{1,14}$", message = "phone must follow E.164 format")
	private String phone;
}
