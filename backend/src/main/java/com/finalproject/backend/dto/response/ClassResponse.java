package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class ClassResponse {
	Long id;
	String name;
	String description;
	LocalDate startDate;
	LocalDate endDate;
	Boolean active;
	Long courseId;
	Long createdBy;
	Instant createdAt;
	List<Long> teacherIds;
}
