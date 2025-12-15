package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserCreationRequest {

	@NotBlank(message = "username must not be blank")
	@Pattern(regexp = "^[A-Za-z0-9_.]{3,32}$", message = "username must be 3-32 characters using letters, digits, underscore or dot")
	private String username;

	@NotBlank(message = "password must not be blank")
	private String password;

	@NotBlank(message = "emailAddress must not be blank")
	@Email
	private String emailAddress;

	@NotBlank(message = "firstName must not be blank")
	private String firstName;

	@NotBlank(message = "lastName must not be blank")
	private String lastName;

	private String emailVisibility;

	private String city;

	private String country;

	private String timezone;

	private String description;

	private String interest;
    private Boolean admin;

	@NotBlank(message = "phoneNumber must not be blank")
	@Pattern(regexp = "^\\d{1,14}$", message = "phoneNumber must follow E.164 format")
	private String phoneNumber;

	private String avatarUrl;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmailVisibility() {
		return emailVisibility;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getDescription() {
		return description;
	}

	public String getInterest() {
		return interest;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}
    public Boolean getAdmin() {return admin;}
}
