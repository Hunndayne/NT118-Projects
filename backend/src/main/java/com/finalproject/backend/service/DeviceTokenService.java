package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.DeviceTokenRequest;
import com.finalproject.backend.entity.DeviceToken;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

	private final DeviceTokenRepository deviceTokenRepository;
	private final UserService userService;

	@Transactional
	public void registerToken(String rawToken, DeviceTokenRequest request) {
		User user = userService.getAuthenticatedUserEntity(rawToken);
		String token = trimRequired(request.getToken(), "token");
		String platform = normalizePlatform(request.getPlatform());
		String deviceId = trimToNull(request.getDeviceId());

		DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
				.orElseGet(() -> DeviceToken.builder().token(token).build());
		deviceToken.setUser(user);
		deviceToken.setPlatform(platform);
		deviceToken.setDeviceId(deviceId);
		deviceToken.setActive(true);
		deviceToken.setLastSeen(Instant.now());
		deviceTokenRepository.save(deviceToken);
	}

	private String normalizePlatform(String platform) {
		String trimmed = trimToNull(platform);
		if (trimmed == null) {
			return "ANDROID";
		}
		String normalized = trimmed.trim().toUpperCase(Locale.US);
		if (normalized.length() > 32) {
			normalized = normalized.substring(0, 32);
		}
		return normalized;
	}

	private String trimRequired(String value, String fieldName) {
		if (value == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
		}
		return trimmed;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
