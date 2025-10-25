package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.LoginRequest;
import com.finalproject.backend.dto.response.LoginResponse;
import com.finalproject.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;

	@PostMapping("/auth/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return userService.login(request);
	}
}
