package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LessonCreateRequest {

	@NotBlank
	private String title;

	private String description;

	private Integer orderIndex;

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Integer getOrderIndex() {
		return orderIndex;
	}
}
