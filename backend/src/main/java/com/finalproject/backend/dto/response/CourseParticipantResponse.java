package com.finalproject.backend.dto.response;

public class CourseParticipantResponse {

	private final Long id;
	private final String firstName;
	private final String lastName;

	public CourseParticipantResponse(Long id, String firstName, String lastName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
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
}
