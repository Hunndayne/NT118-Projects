package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.PresignUploadRequest;
import com.finalproject.backend.dto.response.PresignUploadResponse;
import com.finalproject.backend.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

	private final FileService fileService;

	@PostMapping("/presign-upload")
	public PresignUploadResponse presignUpload(@RequestHeader("X-Auth-Token") String token,
	                                           @Valid @RequestBody PresignUploadRequest request) {
		return fileService.presignUpload(token, request);
	}
}
