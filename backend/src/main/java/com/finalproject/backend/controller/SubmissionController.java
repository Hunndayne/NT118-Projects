package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.GradeRequest;
import com.finalproject.backend.dto.request.SubmissionRequest;
import com.finalproject.backend.dto.response.SubmissionResponse;
import com.finalproject.backend.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class SubmissionController {

	private final SubmissionService submissionService;

	@PostMapping
	public SubmissionResponse submit(@RequestHeader("X-Auth-Token") String token,
	                                 @PathVariable("assignmentId") Long assignmentId,
	                                 @Valid @RequestBody SubmissionRequest request) {
		return submissionService.submitAssignment(token, assignmentId, request);
	}

	@GetMapping("/{submissionId}")
	public SubmissionResponse getSubmission(@RequestHeader("X-Auth-Token") String token,
	                                        @PathVariable("assignmentId") Long assignmentId,
	                                        @PathVariable("submissionId") Long submissionId) {
		return submissionService.getSubmission(token, assignmentId, submissionId);
	}

	@PutMapping("/{submissionId}/grade")
	public SubmissionResponse grade(@RequestHeader("X-Auth-Token") String token,
	                                @PathVariable("assignmentId") Long assignmentId,
	                                @PathVariable("submissionId") Long submissionId,
	                                @Valid @RequestBody GradeRequest request) {
		return submissionService.gradeSubmission(token, assignmentId, submissionId, request);
	}
}
