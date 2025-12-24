package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.Size;

public class AssignmentResourceRequest {
    @Size(max = 20)
    private String type;
    private String title;
    private String content;
    private String url;
    private String filePath;

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return filePath;
    }
}
