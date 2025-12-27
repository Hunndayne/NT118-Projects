package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.ForgotPasswordRequest;
import com.finalproject.backend.dto.request.LoginRequest;
import com.finalproject.backend.dto.request.ResetPasswordRequest;
import com.finalproject.backend.dto.response.MessageResponse;
import com.finalproject.backend.dto.response.LoginResponse;
import com.finalproject.backend.dto.response.TokenStatusResponse;
import com.finalproject.backend.service.PasswordResetService;
import com.finalproject.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final PasswordResetService passwordResetService;

	@PostMapping("/auth/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return userService.login(request);
	}

	@PostMapping("/auth/forgot-password")
	public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return passwordResetService.requestPasswordReset(request);
	}

	@PostMapping("/auth/reset-password")
	public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		return passwordResetService.resetPassword(request);
	}

	@GetMapping("/checklogin")
	public TokenStatusResponse checkLogin(@RequestHeader("X-Auth-Token") String token) {
		return userService.checkToken(token);
	}

	@PostMapping("/auth/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@RequestHeader("X-Auth-Token") String token) {
		userService.logout(token);
	}
}
