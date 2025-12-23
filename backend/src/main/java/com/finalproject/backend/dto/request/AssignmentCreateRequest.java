package com.finalproject.backend.dto.request;

public class AssignmentCreateRequest {

	private String title;
	private String description;
	private String attachmentUrl;
	private String deadline;
	private String startTime;

	public AssignmentCreateRequest() {
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

	public String getDeadline() {
		return deadline;
	}

	public String getStartTime() {
		return startTime;
	}
}
