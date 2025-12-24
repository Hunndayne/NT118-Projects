package com.finalproject.backend.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileUploadResponse {
    String fileUrl;
    String originalName;
}
