package com.finalproject.backend.dto.response;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class CourseResponse {

	private final Long id;
	private final String code;
	private final String name;
	private final String level;
	private final String description;
	private final Boolean active;
	private final Long createdBy;
	private final Instant createdAt;
	private final int lessonCount;
	private final DayOfWeek dayOfWeek;
	private final LocalTime startTime;
	private final LocalTime endTime;
	private final LocalDate startDate;
	private final LocalDate endDate;

	public CourseResponse(Long id,
	                      String code,
	                      String name,
	                      String level,
	                      String description,
	                      Boolean active,
	                      Long createdBy,
	                      Instant createdAt,
	                      int lessonCount,
	                      DayOfWeek dayOfWeek,
	                      LocalTime startTime,
	                      LocalTime endTime,
	                      LocalDate startDate,
	                      LocalDate endDate) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.level = level;
		this.description = description;
		this.active = active;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.lessonCount = lessonCount;
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}

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

	public Long getCreatedBy() {
		return createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public int getLessonCount() {
		return lessonCount;
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
