package com.finalproject.backend.dto.response;

import java.time.Instant;

public class CourseResponse {

	private final Long id;
	private final String code;
	private final String name;
	private final String level;
	private final String description;
	private final Boolean active;
	private final Long createdBy;
	private final Instant createdAt;

	public CourseResponse(Long id,
	                      String code,
	                      String name,
	                      String level,
	                      String description,
	                      Boolean active,
	                      Long createdBy,
	                      Instant createdAt) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.level = level;
		this.description = description;
		this.active = active;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
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
}
