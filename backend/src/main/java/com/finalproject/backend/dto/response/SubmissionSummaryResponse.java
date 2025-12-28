package com.finalproject.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionSummaryResponse {
    private int newSubmissions;
    private int pendingReview;
    private String latestSubmissionInfo;
    private String latestPendingInfo;
}
