package com.finalproject.backend.dto.request;

public class AssignmentUpdateRequest {

	private String title;
	private String description;
	private String attachmentUrl;
	private String deadline;

	public AssignmentUpdateRequest() {
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
}
