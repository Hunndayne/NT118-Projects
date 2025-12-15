package com.finalproject.backend.dto.request;

public class LessonUpdateRequest {

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
