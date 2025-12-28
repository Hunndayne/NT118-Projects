package com.example.enggo.admin;

import com.google.gson.annotations.SerializedName;

public class CourseAdmin {
    private long id;
    private String name;
    @SerializedName("code")
    private String classCode;
    private int lessonCount;
    @SerializedName("dayOfWeek")
    private String dayOfWeek;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("endTime")
    private String endTime;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;

    public long getId() { return id; }
    public String getName() { return name; }
    public String getClassCode() { return classCode; }
    public int getLessonCount() { return lessonCount; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}
