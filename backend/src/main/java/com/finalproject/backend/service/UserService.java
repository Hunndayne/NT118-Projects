package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.LoginRequest;
import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.dto.response.LoginResponse;
import com.finalproject.backend.dto.response.UserResponse;
import com.finalproject.backend.entity.AuthToken;
import com.finalproject.backend.entity.TokenType;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.AuthTokenRepository;
import com.finalproject.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final Duration ACCESS_TOKEN_TTL = Duration.ofDays(1);

	private final UserRepository userRepository;
	private final AuthTokenRepository authTokenRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UserResponse createUser(UserCreationRequest request) {
		String username = request.getUsername().trim();
		String email = request.getEmail().trim();
		String fullName = request.getFullName().trim();
		String phone = Optional.ofNullable(request.getPhone())
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.orElse(null);

		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}

		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
		}

		if (phone != null && userRepository.existsByPhone(phone)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already exists");
		}

		User user = User.builder()
				.username(username)
				.email(email)
				.fullName(fullName)
				.phone(phone)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.active(true)
				.admin(false)
				.build();
		user.setLegacyUserId(generateTemporaryLegacyId());

		User saved = userRepository.save(user);

		if (saved.getLegacyUserId() == null) {
			saved.setLegacyUserId(saved.getId());
			saved = userRepository.save(saved);
		} else if (!saved.getLegacyUserId().equals(saved.getId())) {
			saved.setLegacyUserId(saved.getId());
			saved = userRepository.save(saved);
		}

		return toResponse(saved);
	}

	public UserResponse getUserByToken(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication token");
		}

		String tokenHash = hashToken(rawToken);
		AuthToken authToken = authTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
				.filter(token -> token.getExpiresAt().isAfter(Instant.now()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));

		User user = authToken.getUser();

		if (!user.isActive()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
		}

		return toResponse(user);
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		String username = request.getUsername().trim();
		String rawPassword = request.getPassword();

		authTokenRepository.deleteByExpiresAtBefore(Instant.now());

		User user = userRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

		if (!user.isActive()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
		}

		if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}

		Instant now = Instant.now();
		Instant expiresAt = now.plus(ACCESS_TOKEN_TTL);
		String rawToken = UUID.randomUUID().toString();

		AuthToken authToken = AuthToken.builder()
				.user(user)
				.tokenHash(hashToken(rawToken))
				.tokenJti(UUID.randomUUID())
				.type(TokenType.ACCESS)
				.revoked(false)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.build();

		authTokenRepository.save(authToken);

		user.setLastLoginAt(now);
		userRepository.save(user);

		return LoginResponse.builder()
				.token(rawToken)
				.tokenType(TokenType.ACCESS.getDbValue())
				.expiresAt(expiresAt)
				.build();
	}

	private UserResponse toResponse(User user) {
		Long publicId = user.getLegacyUserId();
		if (publicId == null || publicId <= 0) {
			publicId = user.getId();
		}
		return UserResponse.builder()
				.id(publicId)
				.username(user.getUsername())
				.email(user.getEmail())
				.phone(user.getPhone())
				.fullName(user.getFullName())
				.active(user.isActive())
				.admin(user.isAdmin())
				.createdAt(user.getCreatedAt())
				.lastLoginAt(user.getLastLoginAt())
				.build();
	}

	private String hashToken(String token) {
		if (token == null || token.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token");
		}
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] digest = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm is not available", e);
		}
	}

	private long generateTemporaryLegacyId() {
		long candidate = -Math.abs(ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
		// tránh rơi vào 0
		if (candidate == 0L) {
			return -1L;
		}
		return candidate;
	}
}
