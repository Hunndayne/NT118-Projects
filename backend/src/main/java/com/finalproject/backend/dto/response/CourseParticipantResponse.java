package com.finalproject.backend.dto.response;

public class CourseParticipantResponse {

	private final Long id;
	private final String firstName;
	private final String lastName;
	private final String role;
	private final boolean active;

	public CourseParticipantResponse(Long id, String firstName, String lastName, String role, boolean active) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}
}
