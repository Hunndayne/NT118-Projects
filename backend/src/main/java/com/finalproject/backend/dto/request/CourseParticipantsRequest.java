package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CourseParticipantsRequest {

	@NotEmpty(message = "userIds must not be empty")
	private List<@NotNull(message = "userId must not be null") Long> userIds;

	public List<Long> getUserIds() {
		return userIds;
	}
}
