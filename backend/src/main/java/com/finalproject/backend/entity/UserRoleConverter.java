package com.finalproject.backend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
	@Override
	public String convertToDatabaseColumn(UserRole attribute) {
		if (attribute == null) {
			return UserRole.STUDENT.getDbValue();
		}
		return attribute.getDbValue();
	}

	@Override
	public UserRole convertToEntityAttribute(String dbData) {
		return UserRole.fromDbValue(dbData);
	}
}
