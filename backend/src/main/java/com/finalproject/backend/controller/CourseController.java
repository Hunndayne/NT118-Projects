package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.CourseCreationRequest;
import com.finalproject.backend.dto.request.CourseParticipantsRequest;
import com.finalproject.backend.dto.request.CourseUpdateRequest;
import com.finalproject.backend.dto.response.CourseResponse;
import com.finalproject.backend.dto.response.CourseParticipantResponse;
import com.finalproject.backend.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;

	@GetMapping
	public List<CourseResponse> getCourses(@RequestHeader("X-Auth-Token") String token) {
		return courseService.getCoursesForCurrentUser(token);
	}

	@GetMapping("/{id}")
	public CourseResponse getCourse(@RequestHeader("X-Auth-Token") String token,
	                                @PathVariable("id") Long courseId) {
		return courseService.getCourseForCurrentUser(token, courseId);
	}

	@GetMapping("/{id}/participants")
	public List<CourseParticipantResponse> getParticipants(@RequestHeader("X-Auth-Token") String token,
	                                                       @PathVariable("id") Long courseId) {
		return courseService.getParticipantsForCourse(token, courseId);
	}

	@GetMapping("/{id}/eligible-participants")
	public List<CourseParticipantResponse> getEligibleParticipants(@RequestHeader("X-Auth-Token") String token,
	                                                               @PathVariable("id") Long courseId) {
		return courseService.getEligibleParticipants(token, courseId);
	}

	@PostMapping
	public CourseResponse createCourse(@RequestHeader("X-Auth-Token") String token,
	                                   @Valid @RequestBody CourseCreationRequest request) {
		return courseService.createCourse(token, request);
	}

	@PutMapping("/{id}")
	public CourseResponse updateCourse(@RequestHeader("X-Auth-Token") String token,
	                                   @PathVariable("id") Long courseId,
	                                   @Valid @RequestBody CourseUpdateRequest request) {
		return courseService.updateCourse(token, courseId, request);
	}

	@DeleteMapping("/{id}")
	public void deleteCourse(@RequestHeader("X-Auth-Token") String token,
	                         @PathVariable("id") Long courseId) {
		courseService.deleteCourse(token, courseId);
	}

	@PostMapping("/{id}/participants")
	public CourseResponse addParticipants(@RequestHeader("X-Auth-Token") String token,
	                                      @PathVariable("id") Long courseId,
	                                      @Valid @RequestBody CourseParticipantsRequest request) {
		return courseService.addParticipants(token, courseId, request);
	}

	@DeleteMapping("/{id}/participants")
	public CourseResponse removeParticipants(@RequestHeader("X-Auth-Token") String token,
	                                         @PathVariable("id") Long courseId,
	                                         @Valid @RequestBody CourseParticipantsRequest request) {
		return courseService.removeParticipants(token, courseId, request);
	}
}
