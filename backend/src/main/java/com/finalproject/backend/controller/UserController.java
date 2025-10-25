package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.dto.response.UserResponse;
import com.finalproject.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public UserResponse createUser(@Valid @RequestBody UserCreationRequest request) {
		return userService.createUser(request);
	}

	@GetMapping
	public UserResponse getCurrentUser(@RequestHeader("X-Auth-Token") String token) {
		return userService.getUserByToken(token);
	}
}
