package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Value
@Builder
public class CalendarEventResponse {
	Long id;
	String title;
	String description;
	OffsetDateTime deadline;
	Long classId;
	String className;
	Long courseId;
	String courseName;
	BigDecimal weight;
	Long createdBy;
	Instant createdAt;
}
