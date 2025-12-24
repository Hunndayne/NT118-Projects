package com.example.enggo.teacher;

public class AssignmentResourceRequest {
    public String type;
    public String title;
    public String content;
    public String url;
    public String filePath;

    public AssignmentResourceRequest(String type, String title, String content, String url, String filePath) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.url = url;
        this.filePath = filePath;
    }
}
