package com.finalproject.backend.dto.request;

import java.time.LocalDate;

public class ClassUpdateRequest {

	private String name;
	private String description;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean active;

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
