package com.example.enggo.admin;

import com.google.gson.annotations.SerializedName;

public class CourseAdmin {
    private long id;
    private String name;
    @SerializedName("code")
    private String classCode;
    private int lessonCount;

    public long getId() { return id; }
    public String getName() { return name; }
    public String getClassCode() { return classCode; }
    public int getLessonCount() { return lessonCount; }
}
