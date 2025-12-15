package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.LessonCreateRequest;
import com.finalproject.backend.dto.request.LessonResourceRequest;
import com.finalproject.backend.dto.request.LessonUpdateRequest;
import com.finalproject.backend.dto.response.LessonResourceResponse;
import com.finalproject.backend.dto.response.LessonResponse;
import com.finalproject.backend.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes/{classId}/lessons")
@RequiredArgsConstructor
public class LessonController {

	private final LessonService lessonService;

	@GetMapping
	public List<LessonResponse> getLessons(@RequestHeader("X-Auth-Token") String token,
	                                       @PathVariable("classId") Long classId) {
		return lessonService.getLessons(token, classId);
	}

	@GetMapping("/{lessonId}")
	public LessonResponse getLesson(@RequestHeader("X-Auth-Token") String token,
	                                @PathVariable("classId") Long classId,
	                                @PathVariable("lessonId") Long lessonId) {
		return lessonService.getLesson(token, classId, lessonId);
	}

	@PostMapping
	public LessonResponse createLesson(@RequestHeader("X-Auth-Token") String token,
	                                   @PathVariable("classId") Long classId,
	                                   @Valid @RequestBody LessonCreateRequest request) {
		return lessonService.createLesson(token, classId, request);
	}

	@PutMapping("/{lessonId}")
	public LessonResponse updateLesson(@RequestHeader("X-Auth-Token") String token,
	                                   @PathVariable("classId") Long classId,
	                                   @PathVariable("lessonId") Long lessonId,
	                                   @Valid @RequestBody LessonUpdateRequest request) {
		return lessonService.updateLesson(token, classId, lessonId, request);
	}

	@DeleteMapping("/{lessonId}")
	public void deleteLesson(@RequestHeader("X-Auth-Token") String token,
	                         @PathVariable("classId") Long classId,
	                         @PathVariable("lessonId") Long lessonId) {
		lessonService.deleteLesson(token, classId, lessonId);
	}

	@GetMapping("/{lessonId}/resources")
	public List<LessonResourceResponse> getResources(@RequestHeader("X-Auth-Token") String token,
	                                                 @PathVariable("classId") Long classId,
	                                                 @PathVariable("lessonId") Long lessonId) {
		return lessonService.getResources(token, classId, lessonId);
	}

	@PostMapping("/{lessonId}/resources")
	public LessonResourceResponse addResource(@RequestHeader("X-Auth-Token") String token,
	                                          @PathVariable("classId") Long classId,
	                                          @PathVariable("lessonId") Long lessonId,
	                                          @Valid @RequestBody LessonResourceRequest request) {
		return lessonService.addResource(token, classId, lessonId, request);
	}

	@PutMapping("/{lessonId}/resources/{resourceId}")
	public LessonResourceResponse updateResource(@RequestHeader("X-Auth-Token") String token,
	                                             @PathVariable("classId") Long classId,
	                                             @PathVariable("lessonId") Long lessonId,
	                                             @PathVariable("resourceId") Long resourceId,
	                                             @Valid @RequestBody LessonResourceRequest request) {
		return lessonService.updateResource(token, classId, lessonId, resourceId, request);
	}

	@DeleteMapping("/{lessonId}/resources/{resourceId}")
	public void deleteResource(@RequestHeader("X-Auth-Token") String token,
	                           @PathVariable("classId") Long classId,
	                           @PathVariable("lessonId") Long lessonId,
	                           @PathVariable("resourceId") Long resourceId) {
		lessonService.deleteResource(token, classId, lessonId, resourceId);
	}
}
