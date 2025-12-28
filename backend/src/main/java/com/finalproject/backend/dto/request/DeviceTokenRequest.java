package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DeviceTokenRequest {

	@NotBlank(message = "token must not be blank")
	private String token;

	private String platform;

	private String deviceId;

	public String getToken() {
		return token;
	}

	public String getPlatform() {
		return platform;
	}

	public String getDeviceId() {
		return deviceId;
	}
}
