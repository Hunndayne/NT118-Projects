package com.finalproject.backend.entity;

public enum UserRole {
	ADMIN,
	TEACHER,
	STUDENT;

	public boolean isSuperAdmin() {
		return this == ADMIN;
	}

	public boolean isTeacher() {
		return this == TEACHER;
	}

	public boolean isStudent() {
		return this == STUDENT;
	}
}
