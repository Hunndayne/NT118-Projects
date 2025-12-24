package com.example.enggo.teacher;

public class SubmissionStatusResponse {
    public Long studentId;
    public String firstName;
    public String lastName;
    public boolean submitted;
    public String submittedAt;
    public Double score;
    public String status;
    public String fileUrl;
    public String deadline;

    public String getDisplayName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? "Unknown" : combined;
    }
}
