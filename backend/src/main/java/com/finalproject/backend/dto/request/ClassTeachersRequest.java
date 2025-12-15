package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ClassTeachersRequest {

	@NotEmpty
	private List<Long> teacherIds;

	public List<Long> getTeacherIds() {
		return teacherIds;
	}
}
