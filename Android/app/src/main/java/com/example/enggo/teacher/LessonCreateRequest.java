package com.example.enggo.teacher;

public class LessonCreateRequest {
    public String title;
    public String description;
    public Integer orderIndex;

    public LessonCreateRequest(String title, String description, Integer orderIndex) {
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}
