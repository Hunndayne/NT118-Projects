package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.Size;

public class CourseUpdateRequest {

	@Size(max = 64, message = "code must be at most 64 characters")
	private String code;

	@Size(max = 255, message = "name must be at most 255 characters")
	private String name;

	@Size(max = 64, message = "level must be at most 64 characters")
	private String level;

	private String description;

	private Boolean active;

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getActive() {
		return active;
	}
}
