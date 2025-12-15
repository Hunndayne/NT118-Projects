package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class LessonResponse {
	Long id;
	String title;
	String description;
	Integer orderIndex;
	Long classId;
	Long createdBy;
	Instant createdAt;
	Instant updatedAt;
	List<LessonResourceResponse> resources;
}
