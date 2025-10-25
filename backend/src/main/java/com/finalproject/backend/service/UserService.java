package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.LoginRequest;
import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.dto.response.LoginResponse;
import com.finalproject.backend.dto.response.UserResponse;
import com.finalproject.backend.entity.AuthToken;
import com.finalproject.backend.entity.TokenType;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.entity.UserProfile;
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
		String username = trimRequired(request.getUsername(), "username");
		String emailAddress = trimRequired(request.getEmailAddress(), "emailAddress");
		String firstName = trimRequired(request.getFirstName(), "firstName");
		String lastName = trimRequired(request.getLastName(), "lastName");
		String phoneNumber = trimRequired(request.getPhoneNumber(), "phoneNumber");
		String fullName = (firstName + " " + lastName).replaceAll("\\s+", " ").trim();
		String emailVisibility = trimToNull(request.getEmailVisibility());
		String city = trimToNull(request.getCity());
		String country = trimToNull(request.getCountry());
		String timezone = trimToNull(request.getTimezone());
		String description = trimToNull(request.getDescription());
		String interest = trimToNull(request.getInterest());
		String avatarUrl = trimToNull(request.getAvatarUrl());

		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		}

		if (userRepository.existsByEmailIgnoreCase(emailAddress)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
		}

		boolean phoneConflict = userRepository.existsByPhoneNumber(phoneNumber);
		if (!phoneConflict) {
			phoneConflict = userRepository.existsByPhone(phoneNumber);
		}
		if (phoneConflict) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already exists");
		}

		User user = User.builder()
				.username(username)
				.email(emailAddress)
				.emailAddress(emailAddress)
				.firstName(firstName)
				.lastName(lastName)
				.fullName(fullName)
				.emailVisibility(emailVisibility)
				.city(city)
				.country(country)
				.timezone(timezone)
				.description(description)
				.interest(interest)
				.phoneNumber(phoneNumber)
				.phone(phoneNumber)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.active(true)
				.admin(false)
				.build();

		if (avatarUrl != null) {
			UserProfile profile = new UserProfile();
			profile.setAvatarUrl(avatarUrl);
			profile.setUser(user);
			user.setProfile(profile);
		}

		user.setLegacyUserId(generateTemporaryLegacyId());

		User saved = userRepository.save(user);

		if (saved.getLegacyUserId() == null || saved.getLegacyUserId() <= 0) {
			saved.setLegacyUserId(saved.getId());
			saved = userRepository.save(saved);
		}

		User hydrated = loadUserWithProfile(saved.getId());
		return toResponse(hydrated);
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

		User hydrated = loadUserWithProfile(user.getId());
		return toResponse(hydrated);
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		String username = trimRequired(request.getUsername(), "username");
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

		UserProfile profile = user.getProfile();
		String avatarUrl = profile != null ? trimToNull(profile.getAvatarUrl()) : null;
		String emailAddress = Optional.ofNullable(trimToNull(user.getEmailAddress()))
				.orElseGet(() -> trimToNull(user.getEmail()));
		String phoneNumber = Optional.ofNullable(trimToNull(user.getPhoneNumber()))
				.orElseGet(() -> trimToNull(user.getPhone()));

		return UserResponse.builder()
				.id(publicId)
				.username(user.getUsername())
				.firstName(trimToNull(user.getFirstName()))
				.lastName(trimToNull(user.getLastName()))
				.fullName(trimToNull(user.getFullName()))
				.emailAddress(emailAddress)
				.emailVisibility(trimToNull(user.getEmailVisibility()))
				.city(trimToNull(user.getCity()))
				.country(trimToNull(user.getCountry()))
				.timezone(trimToNull(user.getTimezone()))
				.description(trimToNull(user.getDescription()))
				.interest(trimToNull(user.getInterest()))
				.phoneNumber(phoneNumber)
				.avatarUrl(avatarUrl)
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
		if (candidate == 0L) {
			return -1L;
		}
		return candidate;
	}

	private User loadUserWithProfile(Long userId) {
		return userRepository.findWithProfileById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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