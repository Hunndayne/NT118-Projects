package com.finalproject.backend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CourseStatusConverter implements AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean attribute) {
		return Boolean.TRUE.equals(attribute) ? "active" : "inactive";
	}

	@Override
	public Boolean convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		return "active".equalsIgnoreCase(dbData);
	}
}
