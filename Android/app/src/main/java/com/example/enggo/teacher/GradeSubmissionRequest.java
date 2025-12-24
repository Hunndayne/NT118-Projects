package com.example.enggo.teacher;

public class GradeSubmissionRequest {
    private final Double score;
    private final String feedback;
    private final String status;

    public GradeSubmissionRequest(Double score, String feedback, String status) {
        this.score = score;
        this.feedback = feedback;
        this.status = status;
    }
}
