package com.example.enggo.teacher;

public class LessonUpdateRequest {
    public String title;
    public String description;
    public Integer orderIndex;

    public LessonUpdateRequest(String title, String description, Integer orderIndex) {
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}
