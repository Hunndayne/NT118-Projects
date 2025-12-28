package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.DeviceTokenRequest;
import com.finalproject.backend.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

	private final DeviceTokenService deviceTokenService;

	@PostMapping
	public void registerDeviceToken(@RequestHeader("X-Auth-Token") String token,
	                                @Valid @RequestBody DeviceTokenRequest request) {
		deviceTokenService.registerToken(token, request);
	}
}
