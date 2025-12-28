package com.finalproject.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class CourseCreationRequest {

	@NotBlank(message = "code must not be blank")
	@Size(max = 64, message = "code must be at most 64 characters")
	private String code;

	@NotBlank(message = "name must not be blank")
	@Size(max = 255, message = "name must be at most 255 characters")
	private String name;

	@Size(max = 64, message = "level must be at most 64 characters")
	private String level;

	private String description;

	private Boolean active;

	@NotNull(message = "dayOfWeek must not be null")
	private DayOfWeek dayOfWeek;

	@NotNull(message = "startTime must not be null")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@NotNull(message = "endTime must not be null")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;

	@NotNull(message = "startDate must not be null")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@NotNull(message = "endDate must not be null")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

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

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}
}
