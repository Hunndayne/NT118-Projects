package com.finalproject.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

public class AssignmentResponse {

	private final Long id;
	private final Long classId;
	private final String title;
	private final String description;
	private final String attachmentUrl;
	private final OffsetDateTime deadline;
	private final BigDecimal weight;
	private final Long createdBy;
	private final Instant createdAt;

	public AssignmentResponse(Long id,
	                          Long classId,
	                          String title,
	                          String description,
	                          String attachmentUrl,
	                          OffsetDateTime deadline,
	                          BigDecimal weight,
	                          Long createdBy,
	                          Instant createdAt) {
		this.id = id;
		this.classId = classId;
		this.title = title;
		this.description = description;
		this.attachmentUrl = attachmentUrl;
		this.deadline = deadline;
		this.weight = weight;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public Long getClassId() {
		return classId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getAttachmentUrl() {
		return attachmentUrl;
	}

	public OffsetDateTime getDeadline() {
		return deadline;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
