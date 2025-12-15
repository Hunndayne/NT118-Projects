package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.ClassCreateRequest;
import com.finalproject.backend.dto.request.ClassTeachersRequest;
import com.finalproject.backend.dto.request.ClassUpdateRequest;
import com.finalproject.backend.dto.response.ClassResponse;
import com.finalproject.backend.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassController {

	private final ClassService classService;

	@GetMapping
	public List<ClassResponse> getClasses(@RequestHeader("X-Auth-Token") String token) {
		return classService.getClassesForCurrentUser(token);
	}

	@GetMapping("/{id}")
	public ClassResponse getClassById(@RequestHeader("X-Auth-Token") String token,
	                                  @PathVariable("id") Long classId) {
		return classService.getClassForCurrentUser(token, classId);
	}

	@PostMapping
	public ClassResponse createClass(@RequestHeader("X-Auth-Token") String token,
	                                 @Valid @RequestBody ClassCreateRequest request) {
		return classService.createClass(token, request);
	}

	@PutMapping("/{id}")
	public ClassResponse updateClass(@RequestHeader("X-Auth-Token") String token,
	                                 @PathVariable("id") Long classId,
	                                 @Valid @RequestBody ClassUpdateRequest request) {
		return classService.updateClass(token, classId, request);
	}

	@DeleteMapping("/{id}")
	public void deleteClass(@RequestHeader("X-Auth-Token") String token,
	                        @PathVariable("id") Long classId) {
		classService.deleteClass(token, classId);
	}

	@PostMapping("/{id}/teachers")
	public ClassResponse addTeachers(@RequestHeader("X-Auth-Token") String token,
	                                 @PathVariable("id") Long classId,
	                                 @Valid @RequestBody ClassTeachersRequest request) {
		return classService.addTeachers(token, classId, request);
	}

	@DeleteMapping("/{id}/teachers")
	public ClassResponse removeTeachers(@RequestHeader("X-Auth-Token") String token,
	                                    @PathVariable("id") Long classId,
	                                    @Valid @RequestBody ClassTeachersRequest request) {
		return classService.removeTeachers(token, classId, request);
	}
}
