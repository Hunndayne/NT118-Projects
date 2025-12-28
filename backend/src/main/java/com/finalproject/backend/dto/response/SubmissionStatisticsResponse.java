package com.finalproject.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatisticsResponse {
    private int onTime;
    private int late;
    private int missing;
    private double submissionRate;
}
