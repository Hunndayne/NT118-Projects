package com.example.enggo.admin;

public class UpdateCourseRequest {

    private String name;
    private String code;

    public UpdateCourseRequest(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
