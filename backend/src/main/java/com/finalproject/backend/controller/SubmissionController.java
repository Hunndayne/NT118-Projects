package com.finalproject.backend.controller;

import com.finalproject.backend.dto.response.SubmissionStatusResponse;
import com.finalproject.backend.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/classes/{classId}/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class SubmissionController {

	private final SubmissionService submissionService;

	@GetMapping
	public List<SubmissionStatusResponse> getSubmissionStatus(@RequestHeader("X-Auth-Token") String token,
	                                                          @PathVariable("classId") Long classId,
	                                                          @PathVariable("assignmentId") Long assignmentId) {
		return submissionService.getSubmissionStatus(token, classId, assignmentId);
	}
}
