package com.example.enggo.api;

public class PresignUploadRequest {
    private final String purpose;
    private final String fileName;
    private final String contentType;
    private final Long classId;
    private final Long lessonId;
    private final Long assignmentId;
    public String purpose;
    public String fileName;
    public String contentType;
    public Long classId;
    public Long lessonId;
    public Long assignmentId;

   //public PresignUploadRequest() {
   // }

    public PresignUploadRequest(String purpose,
                                String fileName,
                                String contentType,
                                Long classId,
                                Long lessonId,
                                Long assignmentId) {
        this.purpose = purpose;
        this.fileName = fileName;
        this.contentType = contentType;
        this.classId = classId;
        this.lessonId = lessonId;
        this.assignmentId = assignmentId;
    }
}
