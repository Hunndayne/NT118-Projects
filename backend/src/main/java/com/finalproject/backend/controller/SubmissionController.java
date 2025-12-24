package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.GradeRequest;
import com.finalproject.backend.dto.request.SubmissionRequest;
import com.finalproject.backend.dto.response.SubmissionResponse;
import com.finalproject.backend.dto.response.SubmissionStatusResponse;
import com.finalproject.backend.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/classes/{classId}/assignments/{assignmentId}/submissions")
    public List<SubmissionStatusResponse> getSubmissionStatus(@RequestHeader("X-Auth-Token") String token,
                                                              @PathVariable("classId") Long classId,
                                                              @PathVariable("assignmentId") Long assignmentId) {
        return submissionService.getSubmissionStatus(token, classId, assignmentId);
    }

    @GetMapping("/assignments/{assignmentId}/submissions/me")
    public SubmissionResponse getMySubmission(@RequestHeader("X-Auth-Token") String token,
                                              @PathVariable("assignmentId") Long assignmentId) {
        return submissionService.getMySubmission(token, assignmentId);
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public SubmissionResponse submit(@RequestHeader("X-Auth-Token") String token,
                                     @PathVariable("assignmentId") Long assignmentId,
                                     @Valid @RequestBody SubmissionRequest request) {
        return submissionService.submitAssignment(token, assignmentId, request);
    }

    @GetMapping("/assignments/{assignmentId}/submissions/{submissionId}")
    public SubmissionResponse getSubmission(@RequestHeader("X-Auth-Token") String token,
                                            @PathVariable("assignmentId") Long assignmentId,
                                            @PathVariable("submissionId") Long submissionId) {
        return submissionService.getSubmission(token, assignmentId, submissionId);
    }

    @DeleteMapping("/assignments/{assignmentId}/submissions/me")
    public void deleteMySubmission(@RequestHeader("X-Auth-Token") String token,
                                   @PathVariable("assignmentId") Long assignmentId) {
        submissionService.deleteMySubmission(token, assignmentId);
    }

    @PutMapping("/assignments/{assignmentId}/submissions/{submissionId}/grade")
    public SubmissionResponse grade(@RequestHeader("X-Auth-Token") String token,
                                    @PathVariable("assignmentId") Long assignmentId,
                                    @PathVariable("submissionId") Long submissionId,
                                    @Valid @RequestBody GradeRequest request) {
        return submissionService.gradeSubmission(token, assignmentId, submissionId, request);
    }
}
