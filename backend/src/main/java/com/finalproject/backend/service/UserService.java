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
import com.finalproject.backend.entity.UserRole;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

        UserRole requestedRole = UserRole.STUDENT;
        if (request.getRole() != null) {
            requestedRole = parseRole(request.getRole());
        } else if (Boolean.TRUE.equals(request.getAdmin())) {
            requestedRole = UserRole.SUPER_ADMIN;
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
                .admin(requestedRole.isSuperAdmin())
                .role(requestedRole)
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
        User authenticated = resolveAuthenticatedUser(rawToken);
        return toResponse(authenticated);
    }

    public User getAuthenticatedUserEntity(String rawToken) {
        return resolveAuthenticatedUser(rawToken);
    }

    public User loadUserEntity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        effectiveRole(user);
        return user;
    }

    public UserResponse getUserByIdForAdmin(String rawToken, Long userId) {
        User adminUser = resolveAuthenticatedUser(rawToken);
        if (!adminUser.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
        }

        User targetUser = loadUserWithProfileByIdentifier(userId);
        return toResponse(targetUser);
    }

    @Transactional
    public void lockUser(String rawToken, Long userId) {
        User admin = resolveAuthenticatedUser(rawToken);
        if (!admin.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        User target = loadUserEntity(userId);
        target.setActive(false);
        userRepository.save(target);
    }

    @Transactional
    public void unlockUser(String rawToken, Long userId) {
        User admin = resolveAuthenticatedUser(rawToken);
        if (!admin.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        User target = loadUserEntity(userId);
        target.setActive(true);
        userRepository.save(target);
    }


    @Transactional
    public UserResponse updateUser(String rawToken, Long userIdentifier, UserUpdateRequest request) {
        User actingUser = resolveAuthenticatedUser(rawToken);
        User targetUser = loadUserWithProfileByIdentifier(userIdentifier);

        boolean actorIsAdmin = actingUser.isSuperAdmin();
        boolean sameUser = Objects.equals(actingUser.getId(), targetUser.getId());
        if (!actorIsAdmin && !sameUser) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update this user");
        }

        return applyUserUpdates(actingUser, targetUser, request);
    }

    @Transactional
    public UserResponse updateCurrentUser(String rawToken, UserUpdateRequest request) {
        User actingUser = resolveAuthenticatedUser(rawToken);
        return applyUserUpdates(actingUser, actingUser, request);
    }

    /**
     * ✅ ADMIN: List all active students (non-admin)
     * Uses your existing repository method: findAllStudents()
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllStudents(String rawToken) {
        User admin = resolveAuthenticatedUser(rawToken);

        if (!admin.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
        }

        return userRepository.findAllStudents()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ ADMIN: List all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String rawToken) {
        User admin = resolveAuthenticatedUser(rawToken);

        if (!admin.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
        }

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ ADMIN: Delete user
     * - Requires super admin
     * - Prevent deleting admin
     * - Revoke tokens before delete (recommended)
     */
    @Transactional
    public void deleteUser(String rawToken, Long userId) {
        User admin = resolveAuthenticatedUser(rawToken);

        if (!admin.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required");
        }

        User target = loadUserWithProfileByIdentifier(userId);

        if (target.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete admin user");
        }

        // optional but recommended: remove tokens to avoid FK constraint issues
        authTokenRepository.deleteAllByUser(target);

        userRepository.delete(target);
    }

    private UserResponse applyUserUpdates(User actingUser, User targetUser, UserUpdateRequest request) {
        boolean actorIsAdmin = actingUser.isSuperAdmin();

        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(targetUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be changed");
        }

        if (request.getEmailAddress() != null) {
            String emailAddress = trimRequired(request.getEmailAddress(), "emailAddress");
            userRepository.findByEmailIgnoreCase(emailAddress)
                    .filter(existing -> !Objects.equals(existing.getId(), targetUser.getId()))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                    });
            targetUser.setEmail(emailAddress);
            targetUser.setEmailAddress(emailAddress);
        }

        boolean nameChanged = false;
        if (request.getFirstName() != null) {
            targetUser.setFirstName(trimRequired(request.getFirstName(), "firstName"));
            nameChanged = true;
        }

        if (request.getLastName() != null) {
            targetUser.setLastName(trimRequired(request.getLastName(), "lastName"));
            nameChanged = true;
        }

        if (request.getEmailVisibility() != null) {
            targetUser.setEmailVisibility(trimToNull(request.getEmailVisibility()));
        }

        if (request.getCity() != null) {
            targetUser.setCity(trimToNull(request.getCity()));
        }

        if (request.getCountry() != null) {
            targetUser.setCountry(trimToNull(request.getCountry()));
        }

        if (request.getTimezone() != null) {
            targetUser.setTimezone(trimToNull(request.getTimezone()));
        }

        if (request.getDescription() != null) {
            targetUser.setDescription(trimToNull(request.getDescription()));
        }

        if (request.getInterest() != null) {
            targetUser.setInterest(trimToNull(request.getInterest()));
        }

        if (request.getPhoneNumber() != null) {
            String phoneNumber = trimRequired(request.getPhoneNumber(), "phoneNumber");
            if (!phoneNumber.equals(targetUser.getPhoneNumber())) {
                boolean phoneConflict = userRepository.existsByPhoneNumber(phoneNumber);
                if (!phoneConflict) {
                    phoneConflict = userRepository.existsByPhone(phoneNumber);
                }
                if (phoneConflict) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already exists");
                }
                targetUser.setPhoneNumber(phoneNumber);
                targetUser.setPhone(phoneNumber);
            }
        }

        if (request.getAvatarUrl() != null) {
            String avatarUrl = trimToNull(request.getAvatarUrl());
            UserProfile profile = targetUser.getProfile();
            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(targetUser);
                targetUser.setProfile(profile);
            }
            profile.setAvatarUrl(avatarUrl);
        }

        if (request.getPassword() != null) {
            String rawPassword = request.getPassword();
            if (rawPassword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password must not be blank");
            }
            targetUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        if (request.getRole() != null) {
            if (!actorIsAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin privileges required to change role");
            }
            UserRole newRole = parseRole(request.getRole());
            targetUser.setRole(newRole);
            targetUser.setAdmin(newRole.isSuperAdmin());
        }

        if (nameChanged) {
            recomputeFullName(targetUser);
        }

        User saved = userRepository.save(targetUser);
        User hydrated = loadUserWithProfile(saved.getId());
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

        UserRole role = effectiveRole(user);
        return LoginResponse.builder()
                .token(rawToken)
                .tokenType(TokenType.ACCESS.getDbValue())
                .expiresAt(expiresAt)
                .admin(role.isSuperAdmin())
                .role(role.name())
                .build();
    }

    public TokenStatusResponse checkToken(String rawToken) {
        AuthToken authToken = resolveActiveToken(rawToken);
        User user = authToken.getUser();

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        UserRole role = effectiveRole(user);
        return new TokenStatusResponse(true, role.isSuperAdmin(), authToken.getExpiresAt(), role.name());
    }

    public void logout(String rawToken) {
        AuthToken authToken = resolveActiveToken(rawToken);
        authToken.setRevoked(true);
        authTokenRepository.save(authToken);
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

        UserRole role = effectiveRole(user);

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
                .admin(role.isSuperAdmin())
                .role(role.name())
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
        User user = userRepository.findWithProfileById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        effectiveRole(user);
        return user;
    }

    private User loadUserWithProfileByIdentifier(Long identifier) {
        Optional<User> byId = userRepository.findWithProfileById(identifier);
        if (byId.isPresent()) {
            effectiveRole(byId.get());
            return byId.get();
        }
        User user = userRepository.findWithProfileByLegacyUserId(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        effectiveRole(user);
        return user;
    }

    private void recomputeFullName(User user) {
        String first = trimToNull(user.getFirstName());
        String last = trimToNull(user.getLastName());
        String combined = ((first != null ? first : "") + " " + (last != null ? last : "")).replaceAll("\\s+", " ").trim();
        user.setFullName(combined.isEmpty() ? null : combined);
    }

    private User resolveAuthenticatedUser(String rawToken) {
        AuthToken authToken = resolveActiveToken(rawToken);
        User user = authToken.getUser();

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        return loadUserWithProfile(user.getId());
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.fromString(role);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role value");
        }
    }

    private UserRole effectiveRole(User user) {
        if (user.getRole() != null) {
            return user.getRole();
        }
        UserRole derived = user.isAdmin() ? UserRole.SUPER_ADMIN : UserRole.STUDENT;
        user.setRole(derived);
        return derived;
    }

    private AuthToken resolveActiveToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication token");
        }

        String tokenHash = hashToken(rawToken);
        return authTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
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
