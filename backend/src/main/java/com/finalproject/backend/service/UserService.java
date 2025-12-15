package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.LoginRequest;
import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.dto.request.UserUpdateRequest;
import com.finalproject.backend.dto.response.LoginResponse;
import com.finalproject.backend.dto.response.TokenStatusResponse;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofDays(1);

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // CREATE USER (ADMIN ONLY – ROLE FROM REQUEST)
    // =========================================================
    @Transactional
    public UserResponse createUser(String token, UserCreationRequest request) {

        User admin = getAuthenticatedUserEntity(token);
        if (!admin.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin privileges required"
            );
        }

        String username = trimRequired(request.getUsername(), "username");
        String email = trimRequired(request.getEmailAddress(), "emailAddress");
        String firstName = trimRequired(request.getFirstName(), "firstName");
        String lastName = trimRequired(request.getLastName(), "lastName");
        String phone = trimRequired(request.getPhoneNumber(), "phoneNumber");

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        boolean isAdmin = Boolean.TRUE.equals(request.getAdmin());

        User user = User.builder()
                .username(username)
                .email(email)
                .emailAddress(email)
                .firstName(firstName)
                .lastName(lastName)
                .fullName((firstName + " " + lastName).trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(phone)
                .phoneNumber(phone)
                .active(true)
                .admin(isAdmin)
                .build();

        user.setLegacyUserId(generateTemporaryLegacyId());
        user = userRepository.save(user);

        if (user.getLegacyUserId() <= 0) {
            user.setLegacyUserId(user.getId());
            userRepository.save(user);
        }

        return toResponse(loadUserWithProfile(user.getId()));
    }

    // =========================================================
    // LOGIN (USERNAME + PASSWORD)
    // =========================================================
    @Transactional
    public LoginResponse login(LoginRequest request) {

        String username = trimRequired(request.getUsername(), "username");
        String rawPassword = request.getPassword();

        authTokenRepository.deleteByExpiresAtBefore(Instant.now());

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

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
                .admin(user.isAdmin())
                .build();
    }

    // =========================================================
    // TOKEN / AUTH
    // =========================================================
    public TokenStatusResponse checkToken(String rawToken) {
        AuthToken authToken = resolveActiveToken(rawToken);
        User user = authToken.getUser();

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        return new TokenStatusResponse(true, user.isAdmin(), authToken.getExpiresAt());
    }

    public void logout(String rawToken) {
        AuthToken authToken = resolveActiveToken(rawToken);
        authToken.setRevoked(true);
        authTokenRepository.save(authToken);
    }

    public User getAuthenticatedUserEntity(String rawToken) {
        AuthToken authToken = resolveActiveToken(rawToken);
        User user = authToken.getUser();

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        return loadUserWithProfile(user.getId());
    }

    public UserResponse getUserByToken(String rawToken) {
        return toResponse(getAuthenticatedUserEntity(rawToken));
    }

    // =========================================================
    // ADMIN – GET ALL STUDENTS
    // =========================================================
    public List<UserResponse> getAllStudents(String token) {

        User admin = getAuthenticatedUserEntity(token);

        if (!admin.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin privileges required"
            );
        }

        return userRepository.findAllStudents()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // UPDATE USER
    // =========================================================
    @Transactional
    public UserResponse updateCurrentUser(String rawToken, UserUpdateRequest request) {
        User user = getAuthenticatedUserEntity(rawToken);
        return applyUserUpdates(user, user, request);
    }

    @Transactional
    public UserResponse updateUser(String rawToken, Long userId, UserUpdateRequest request) {
        User actor = getAuthenticatedUserEntity(rawToken);
        User target = loadUserEntity(userId);

        if (!actor.isAdmin() && !Objects.equals(actor.getId(), target.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        return applyUserUpdates(actor, target, request);
    }
    @Transactional
    public void deleteUser(String rawToken, Long userId) {

        User admin = getAuthenticatedUserEntity(rawToken);

        if (!admin.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin privileges required"
            );
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        if (Objects.equals(admin.getId(), target.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin cannot delete himself"
            );
        }

        target.setActive(false);
        userRepository.save(target);
    }



    // =========================================================
    // INTERNAL
    // =========================================================
    private UserResponse applyUserUpdates(User actor, User target, UserUpdateRequest request) {

        if (request.getEmailAddress() != null) {
            target.setEmail(request.getEmailAddress().trim());
            target.setEmailAddress(request.getEmailAddress().trim());
        }

        if (request.getFirstName() != null)
            target.setFirstName(request.getFirstName().trim());

        if (request.getLastName() != null)
            target.setLastName(request.getLastName().trim());

        if (request.getPhoneNumber() != null) {
            target.setPhoneNumber(request.getPhoneNumber().trim());
            target.setPhone(request.getPhoneNumber().trim());
        }

        if (request.getPassword() != null) {
            target.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(target);
        return toResponse(loadUserWithProfile(saved.getId()));
    }

    private AuthToken resolveActiveToken(String rawToken) {
        String hash = hashToken(rawToken);
        return authTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
    }

    public User loadUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User loadUserWithProfile(Long id) {
        return userRepository.findWithProfileById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toResponse(User user) {
        UserProfile profile = user.getProfile();
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .emailAddress(user.getEmailAddress())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .active(user.isActive())
                .admin(user.isAdmin())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
    public UserResponse getUserByIdForAdmin(String rawToken, Long userId) {

        User admin = getAuthenticatedUserEntity(rawToken);

        if (!admin.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin privileges required"
            );
        }

        User target = loadUserWithProfile(userId);
        return toResponse(target);
    }


    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private long generateTemporaryLegacyId() {
        long v = -Math.abs(ThreadLocalRandom.current().nextLong());
        return v == 0 ? -1 : v;
    }

    private String trimRequired(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return v.trim();
    }
}
