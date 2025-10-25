package com.finalproject.backend.entity;

import java.util.Arrays;

public enum TokenType {
	ACCESS("access"),
	REFRESH("refresh"),
	PERSONAL("personal");

	private final String dbValue;

	TokenType(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return dbValue;
	}

	public static TokenType fromDbValue(String value) {
		return Arrays.stream(values())
				.filter(type -> type.dbValue.equalsIgnoreCase(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown token type: " + value));
	}
}
