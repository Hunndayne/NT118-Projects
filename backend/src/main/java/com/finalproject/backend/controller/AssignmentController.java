package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.AssignmentCreateRequest;
import com.finalproject.backend.dto.request.AssignmentResourceRequest;
import com.finalproject.backend.dto.request.AssignmentUpdateRequest;
import com.finalproject.backend.dto.response.AssignmentResponse;
import com.finalproject.backend.dto.response.AssignmentResourceResponse;
import com.finalproject.backend.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes/{classId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

	private final AssignmentService assignmentService;

	@GetMapping
	public List<AssignmentResponse> getAssignments(@RequestHeader("X-Auth-Token") String token,
	                                               @PathVariable("classId") Long classId) {
		return assignmentService.getAssignments(token, classId);
	}

	@GetMapping("/{assignmentId}")
	public AssignmentResponse getAssignment(@RequestHeader("X-Auth-Token") String token,
	                                        @PathVariable("classId") Long classId,
	                                        @PathVariable("assignmentId") Long assignmentId) {
		return assignmentService.getAssignment(token, classId, assignmentId);
	}

	@GetMapping("/{assignmentId}/resources")
	public List<AssignmentResourceResponse> getResources(@RequestHeader("X-Auth-Token") String token,
	                                                     @PathVariable("classId") Long classId,
	                                                     @PathVariable("assignmentId") Long assignmentId) {
		return assignmentService.getResources(token, classId, assignmentId);
	}

	@PostMapping
	public AssignmentResponse createAssignment(@RequestHeader("X-Auth-Token") String token,
	                                           @PathVariable("classId") Long classId,
	                                           @Valid @RequestBody AssignmentCreateRequest request) {
		return assignmentService.createAssignment(token, classId, request);
	}

	@PutMapping("/{assignmentId}")
	public AssignmentResponse updateAssignment(@RequestHeader("X-Auth-Token") String token,
	                                           @PathVariable("classId") Long classId,
	                                           @PathVariable("assignmentId") Long assignmentId,
	                                           @Valid @RequestBody AssignmentUpdateRequest request) {
		return assignmentService.updateAssignment(token, classId, assignmentId, request);
	}

	@PostMapping("/{assignmentId}/resources")
	public AssignmentResourceResponse addResource(@RequestHeader("X-Auth-Token") String token,
	                                              @PathVariable("classId") Long classId,
	                                              @PathVariable("assignmentId") Long assignmentId,
	                                              @Valid @RequestBody AssignmentResourceRequest request) {
		return assignmentService.addResource(token, classId, assignmentId, request);
	}

	@PutMapping("/{assignmentId}/resources/{resourceId}")
	public AssignmentResourceResponse updateResource(@RequestHeader("X-Auth-Token") String token,
	                                                 @PathVariable("classId") Long classId,
	                                                 @PathVariable("assignmentId") Long assignmentId,
	                                                 @PathVariable("resourceId") Long resourceId,
	                                                 @Valid @RequestBody AssignmentResourceRequest request) {
		return assignmentService.updateResource(token, classId, assignmentId, resourceId, request);
	}

	@DeleteMapping("/{assignmentId}/resources/{resourceId}")
	public void deleteResource(@RequestHeader("X-Auth-Token") String token,
	                           @PathVariable("classId") Long classId,
	                           @PathVariable("assignmentId") Long assignmentId,
	                           @PathVariable("resourceId") Long resourceId) {
		assignmentService.deleteResource(token, classId, assignmentId, resourceId);
	}

	@DeleteMapping("/{assignmentId}")
	public void deleteAssignment(@RequestHeader("X-Auth-Token") String token,
	                             @PathVariable("classId") Long classId,
	                             @PathVariable("assignmentId") Long assignmentId) {
		assignmentService.deleteAssignment(token, classId, assignmentId);
	}
}
