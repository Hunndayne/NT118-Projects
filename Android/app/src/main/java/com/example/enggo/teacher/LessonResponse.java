package com.example.enggo.teacher;

import java.util.List;

public class LessonResponse {
    public Long id;
    public String title;
    public String description;
    public Integer orderIndex;
    public List<LessonResourceResponse> resources;
}
