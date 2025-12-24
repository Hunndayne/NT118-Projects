package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AssignmentResourceResponse {
    Long id;
    String type;
    String title;
    String content;
    String url;
    String filePath;
    Instant createdAt;
    Instant updatedAt;
}
