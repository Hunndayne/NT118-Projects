package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.CourseCreationRequest;
import com.finalproject.backend.dto.request.CourseUpdateRequest;
import com.finalproject.backend.dto.response.CourseResponse;
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
}
