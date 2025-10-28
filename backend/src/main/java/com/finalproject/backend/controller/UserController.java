package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.dto.response.UserResponse;
import com.finalproject.backend.dto.request.UserUpdateRequest;
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

	@PutMapping
	public UserResponse updateCurrentUser(@RequestHeader("X-Auth-Token") String token,
	                                      @Valid @RequestBody UserUpdateRequest request) {
		return userService.updateCurrentUser(token, request);
	}

	@GetMapping("/{id}")
	public UserResponse getUserById(@RequestHeader("X-Auth-Token") String token, @PathVariable("id") Long userId) {
		return userService.getUserByIdForAdmin(token, userId);
	}

	@PutMapping("/{id}")
	public UserResponse updateUser(@RequestHeader("X-Auth-Token") String token,
	                               @PathVariable("id") Long userId,
	                               @Valid @RequestBody UserUpdateRequest request) {
		return userService.updateUser(token, userId, request);
	}
}
