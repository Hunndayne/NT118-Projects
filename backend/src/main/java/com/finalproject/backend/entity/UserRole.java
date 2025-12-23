package com.finalproject.backend.entity;

public enum UserRole {
	SUPER_ADMIN("super_admin"),
	TEACHER("teacher"),
	STUDENT("student");

	private final String dbValue;

	UserRole(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return dbValue;
	}

	public boolean isSuperAdmin() {
		return this == SUPER_ADMIN;
	}

	public boolean isTeacher() {
		return this == TEACHER;
	}

	public boolean isStudent() {
		return this == STUDENT;
	}

	public static UserRole fromString(String value) {
		if (value == null) {
			return STUDENT;
		}
		String normalized = value.trim().toLowerCase();
		if ("admin".equals(normalized) || "superadmin".equals(normalized) || "super-admin".equals(normalized)) {
			return SUPER_ADMIN;
		}
		for (UserRole role : values()) {
			if (role.name().equalsIgnoreCase(normalized) || role.dbValue.equalsIgnoreCase(normalized)) {
				return role;
			}
		}
		throw new IllegalArgumentException("Unknown role: " + value);
	}

	public static UserRole fromDbValue(String value) {
		return fromString(value);
	}
}
