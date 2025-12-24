package com.example.enggo.user;

public class SubmissionRequest {
    public String content;
    public String fileUrl;

    public SubmissionRequest(String content, String fileUrl) {
        this.content = content;
        this.fileUrl = fileUrl;
    }
}
