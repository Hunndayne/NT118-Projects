package com.finalproject.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportOverviewResponse {
    private int totalStudents;
    private int totalCourses;
    private int totalClasses;
    private int totalModules;
    private SubmissionStatisticsResponse submissionStatistics;
}
