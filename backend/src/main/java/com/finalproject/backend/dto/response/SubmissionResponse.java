package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class SubmissionResponse {
	Long id;
	Long assignmentId;
	Long studentId;
	Integer attemptNo;
	Instant submittedAt;
	String content;
	String fileUrl;
	String status;
	BigDecimal score;
	String feedback;
	Long gradedBy;
	Instant gradeAt;
}
