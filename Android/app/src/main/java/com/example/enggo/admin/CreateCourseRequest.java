package com.example.enggo.admin;

public class CreateCourseRequest {

    private String name;
    private String code;
    private String description;

    public CreateCourseRequest(String name, String code) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
