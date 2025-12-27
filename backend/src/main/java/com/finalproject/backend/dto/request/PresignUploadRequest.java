package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PresignUploadRequest {

	@NotBlank
	private String purpose;

	@NotBlank
	private String fileName;

	@NotBlank
	private String contentType;

	private Long classId;
	private Long lessonId;
	private Long assignmentId;

	public String getPurpose() {
		return purpose;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public Long getClassId() {
		return classId;
	}

	public Long getLessonId() {
		return lessonId;
	}

	public Long getAssignmentId() {
		return assignmentId;
	}
}
