package com.example.enggo.teacher;

public class AssignmentCreateRequest {
    private final String title;
    private final String description;
    private final String attachmentUrl;
    private final String deadline;
    private final String startTime;

    public AssignmentCreateRequest(String title,
                                   String description,
                                   String attachmentUrl,
                                   String deadline,
                                   String startTime) {
        this.title = title;
        this.description = description;
        this.attachmentUrl = attachmentUrl;
        this.deadline = deadline;
        this.startTime = startTime;
    }
}
