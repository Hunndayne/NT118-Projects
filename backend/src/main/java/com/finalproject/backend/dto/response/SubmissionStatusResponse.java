package com.finalproject.backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class SubmissionStatusResponse {

	private final Long submissionId;
	private final Long studentId;
	private final String firstName;
	private final String lastName;
	private final boolean submitted;
	private final java.time.Instant submittedAt;
	private final BigDecimal score;
	private final String status;
	private final String fileUrl;
	private final OffsetDateTime deadline;

	public SubmissionStatusResponse(Long submissionId,
	                                Long studentId,
	                                String firstName,
	                                String lastName,
	                                boolean submitted,
	                                java.time.Instant submittedAt,
	                                BigDecimal score,
	                                String status,
	                                String fileUrl,
	                                OffsetDateTime deadline) {
		this.submissionId = submissionId;
		this.studentId = studentId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.submitted = submitted;
		this.submittedAt = submittedAt;
		this.score = score;
		this.status = status;
		this.fileUrl = fileUrl;
		this.deadline = deadline;
	}

	public Long getStudentId() {
		return studentId;
	}

	public Long getSubmissionId() {
		return submissionId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public java.time.Instant getSubmittedAt() {
		return submittedAt;
	}

	public BigDecimal getScore() {
		return score;
	}

	public String getStatus() {
		return status;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public OffsetDateTime getDeadline() {
		return deadline;
	}
}
