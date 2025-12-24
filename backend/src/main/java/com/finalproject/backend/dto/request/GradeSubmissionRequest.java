package com.finalproject.backend.dto.request;

public class GradeSubmissionRequest {

	private Double score;
	private String feedback;

	public GradeSubmissionRequest() {
	}

	public Double getScore() {
		return score;
	}

	public String getFeedback() {
		return feedback;
	}
}
