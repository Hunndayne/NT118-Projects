package com.finalproject.backend.entity;

public enum UserRole {
	SUPER_ADMIN,
	TEACHER,
	STUDENT;

	public boolean isSuperAdmin() {
		return this == SUPER_ADMIN;
	}

	public boolean isTeacher() {
		return this == TEACHER;
	}

	public boolean isStudent() {
		return this == STUDENT;
	}
}
