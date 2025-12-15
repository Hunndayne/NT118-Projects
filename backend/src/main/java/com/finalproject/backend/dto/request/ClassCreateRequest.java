package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ClassCreateRequest {

	@NotNull
	private Long courseId;

	@NotBlank
	private String name;

	private String description;

	private LocalDate startDate;

	private LocalDate endDate;

	private Boolean active;

	public Long getCourseId() {
		return courseId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public Boolean getActive() {
		return active;
	}
}
