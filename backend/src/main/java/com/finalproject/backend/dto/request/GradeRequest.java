package com.finalproject.backend.dto.request;

import java.math.BigDecimal;

public class GradeRequest {
	private BigDecimal score;
	private String feedback;
	private String status;

	public BigDecimal getScore() {
		return score;
	}

	public String getFeedback() {
		return feedback;
	}

	public String getStatus() {
		return status;
	}
}
