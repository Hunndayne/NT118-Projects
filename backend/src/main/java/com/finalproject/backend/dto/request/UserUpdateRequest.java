package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
public class UserUpdateRequest {

	@Pattern(regexp = "^[A-Za-z0-9_.]{3,32}$", message = "username must be 3-32 characters using letters, digits, underscore or dot")
	private String username;

	@Email
	private String emailAddress;

	private String firstName;

	private String lastName;

	private String emailVisibility;

	private String city;

	private String country;

	private String timezone;

	private String description;

	private String interest;

	@Pattern(regexp = "^\\d{1,14}$", message = "phoneNumber must follow E.164 format")
	private String phoneNumber;

	private String avatarUrl;

	private String password;

	private Boolean admin;

	public String getUsername() {
		return username;
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

	public String getPassword() {
		return password;
	}

	public Boolean getAdmin() {
		return admin;
	}
}
