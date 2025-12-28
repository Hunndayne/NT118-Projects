package com.example.enggo.admin;

public class CreateCourseRequest {

    private String name;
    private String code;
    private String description;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;

    public CreateCourseRequest(String name, String code, String dayOfWeek,
                               String startTime, String endTime,
                               String startDate, String endDate) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
