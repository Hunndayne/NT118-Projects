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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for UserService
 * Tests cover authentication, user management, authorization, and security-critical operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;
    private AuthToken validToken;
    private String rawToken;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(1L)
                .legacyUserId(1L)
                .username("testuser")
                .email("test@example.com")
                .emailAddress("test@example.com")
                .firstName("Test")
                .lastName("User")
                .fullName("Test User")
                .phoneNumber("1234567890")
                .phone("1234567890")
                .passwordHash("$2a$10$encodedPasswordHash")
                .active(true)
                .admin(false)
                .role(UserRole.STUDENT)
                .createdAt(Instant.now())
                .build();

        // Setup admin user
        adminUser = User.builder()
                .id(2L)
                .legacyUserId(2L)
                .username("admin")
                .email("admin@example.com")
                .emailAddress("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .fullName("Admin User")
                .phoneNumber("0987654321")
                .phone("0987654321")
                .passwordHash("$2a$10$encodedAdminPasswordHash")
                .active(true)
                .admin(true)
                .role(UserRole.ADMIN)
                .createdAt(Instant.now())
                .build();

        // Setup valid auth token
        rawToken = UUID.randomUUID().toString();
        validToken = AuthToken.builder()
                .id(1L)
                .user(testUser)
                .tokenJti(UUID.randomUUID())
                .tokenHash("hashedToken")
                .type(TokenType.ACCESS)
                .revoked(false)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
            when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoginResponse response = userService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isNotBlank();
            assertThat(response.getTokenType()).isEqualTo("access");
            assertThat(response.getAdmin()).isFalse();
            assertThat(response.getRole()).isEqualTo("STUDENT");
            assertThat(response.getExpiresAt()).isAfter(Instant.now());

            // Verify token was saved
            verify(authTokenRepository).save(any(AuthToken.class));
            // Verify user's lastLoginAt was updated
            verify(userRepository).save(argThat(user -> user.getLastLoginAt() != null));
            // Verify expired tokens were cleaned up
            verify(authTokenRepository).deleteByExpiresAtBefore(any(Instant.class));
        }

        @Test
        @DisplayName("Should fail login with invalid username")
        void shouldFailLoginWithInvalidUsername() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword("password123");

            when(userRepository.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Invalid credentials")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);

            verify(authTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail login with incorrect password")
        void shouldFailLoginWithIncorrectPassword() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Invalid credentials")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);

            verify(authTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail login for inactive user")
        void shouldFailLoginForInactiveUser() {
            // Given
            testUser.setActive(false);
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User account is inactive")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(passwordEncoder, never()).matches(any(), any());
            verify(authTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle case-insensitive username login")
        void shouldHandleCaseInsensitiveUsername() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("TESTUSER");
            request.setPassword("password123");

            when(userRepository.findByUsernameIgnoreCase("TESTUSER")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", testUser.getPasswordHash())).thenReturn(true);
            when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoginResponse response = userService.login(request);

            // Then
            assertThat(response).isNotNull();
            verify(userRepository).findByUsernameIgnoreCase("TESTUSER");
        }

        @Test
        @DisplayName("Should fail login with blank username after trim")
        void shouldFailLoginWithBlankUsername() {
            // Given
            LoginRequest request = new LoginRequest();
            request.setUsername("   ");
            request.setPassword("password123");

            // When & Then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("must not be blank")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        private UserCreationRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new UserCreationRequest() {
                {
                    setUsername("newuser");
                    setPassword("password123");
                    setEmailAddress("newuser@example.com");
                    setFirstName("New");
                    setLastName("User");
                    setPhoneNumber("5551234567");
                }

                private String username;
                private String password;
                private String emailAddress;
                private String firstName;
                private String lastName;
                private String phoneNumber;
                private String avatarUrl;

                public void setUsername(String username) { this.username = username; }
                public void setPassword(String password) { this.password = password; }
                public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
                public void setFirstName(String firstName) { this.firstName = firstName; }
                public void setLastName(String lastName) { this.lastName = lastName; }
                public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
                public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

                @Override public String getUsername() { return username; }
                @Override public String getPassword() { return password; }
                @Override public String getEmailAddress() { return emailAddress; }
                @Override public String getFirstName() { return firstName; }
                @Override public String getLastName() { return lastName; }
                @Override public String getPhoneNumber() { return phoneNumber; }
                @Override public String getAvatarUrl() { return avatarUrl; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public Boolean getAdmin() { return null; }
            };
        }

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfully() {
            // Given
            when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
            when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(false);
            when(userRepository.existsByPhoneNumber("5551234567")).thenReturn(false);
            when(userRepository.existsByPhone("5551234567")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedHash");

            User savedUser = User.builder()
                    .id(10L)
                    .legacyUserId(10L)
                    .username("newuser")
                    .email("newuser@example.com")
                    .emailAddress("newuser@example.com")
                    .firstName("New")
                    .lastName("User")
                    .fullName("New User")
                    .phoneNumber("5551234567")
                    .phone("5551234567")
                    .passwordHash("$2a$10$encodedHash")
                    .active(true)
                    .admin(false)
                    .role(UserRole.STUDENT)
                    .createdAt(Instant.now())
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userRepository.findWithProfileById(10L)).thenReturn(Optional.of(savedUser));

            // When
            UserResponse response = userService.createUser(validRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("newuser");
            assertThat(response.getEmailAddress()).isEqualTo("newuser@example.com");
            assertThat(response.getFirstName()).isEqualTo("New");
            assertThat(response.getLastName()).isEqualTo("User");
            assertThat(response.getFullName()).isEqualTo("New User");
            assertThat(response.getPhoneNumber()).isEqualTo("5551234567");
            assertThat(response.getRole()).isEqualTo("STUDENT");
            assertThat(response.getAdmin()).isFalse();
            assertThat(response.getActive()).isTrue();

            verify(passwordEncoder).encode("password123");
            verify(userRepository, atLeast(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should fail when username already exists")
        void shouldFailWhenUsernameExists() {
            // Given
            when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(validRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Username already exists")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when email already exists")
        void shouldFailWhenEmailExists() {
            // Given
            when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
            when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(validRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Email already exists")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when phone number already exists")
        void shouldFailWhenPhoneExists() {
            // Given
            when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
            when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(false);
            when(userRepository.existsByPhoneNumber("5551234567")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(validRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Phone already exists")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should generate correct full name from first and last name")
        void shouldGenerateFullName() {
            // Given
            when(userRepository.existsByUsernameIgnoreCase(any())).thenReturn(false);
            when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
            when(userRepository.existsByPhoneNumber(any())).thenReturn(false);
            when(userRepository.existsByPhone(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encoded");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(10L);
                return user;
            });
            when(userRepository.findWithProfileById(any())).thenAnswer(invocation -> {
                User captured = userCaptor.getValue();
                return Optional.of(captured);
            });

            // When
            userService.createUser(validRequest);

            // Then
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getFullName()).isEqualTo("New User");
        }

        @Test
        @DisplayName("Should trim and validate required fields")
        void shouldTrimAndValidateRequiredFields() {
            // Given - Create request with spaces
            validRequest = new UserCreationRequest() {
                {
                    setUsername("  newuser  ");
                    setPassword("password123");
                    setEmailAddress("  newuser@example.com  ");
                    setFirstName("  New  ");
                    setLastName("  User  ");
                    setPhoneNumber("  5551234567  ");
                }

                private String username;
                private String password;
                private String emailAddress;
                private String firstName;
                private String lastName;
                private String phoneNumber;

                public void setUsername(String username) { this.username = username; }
                public void setPassword(String password) { this.password = password; }
                public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
                public void setFirstName(String firstName) { this.firstName = firstName; }
                public void setLastName(String lastName) { this.lastName = lastName; }
                public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

                @Override public String getUsername() { return username; }
                @Override public String getPassword() { return password; }
                @Override public String getEmailAddress() { return emailAddress; }
                @Override public String getFirstName() { return firstName; }
                @Override public String getLastName() { return lastName; }
                @Override public String getPhoneNumber() { return phoneNumber; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public Boolean getAdmin() { return null; }
            };

            when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
            when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(false);
            when(userRepository.existsByPhoneNumber("5551234567")).thenReturn(false);
            when(userRepository.existsByPhone("5551234567")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encoded");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(10L);
                return user;
            });
            when(userRepository.findWithProfileById(any())).thenAnswer(invocation -> Optional.of(userCaptor.getValue()));

            // When
            userService.createUser(validRequest);

            // Then
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getUsername()).isEqualTo("newuser");
            assertThat(capturedUser.getEmailAddress()).isEqualTo("newuser@example.com");
            assertThat(capturedUser.getFirstName()).isEqualTo("New");
            assertThat(capturedUser.getLastName()).isEqualTo("User");
            assertThat(capturedUser.getPhoneNumber()).isEqualTo("5551234567");
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        private String adminToken;
        private AuthToken adminAuthToken;

        @BeforeEach
        void setUp() {
            adminToken = UUID.randomUUID().toString();
            adminAuthToken = AuthToken.builder()
                    .id(2L)
                    .user(adminUser)
                    .tokenHash("adminTokenHash")
                    .tokenJti(UUID.randomUUID())
                    .type(TokenType.ACCESS)
                    .revoked(false)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();
        }

        @Test
        @DisplayName("Should allow user to update their own information")
        void shouldAllowUserToUpdateOwnInfo() {
            // Given
            UserUpdateRequest request = new UserUpdateRequest() {
                {
                    setFirstName("Updated");
                    setCity("New York");
                }

                private String firstName;
                private String city;

                public void setFirstName(String firstName) { this.firstName = firstName; }
                public void setCity(String city) { this.city = city; }

                @Override public String getFirstName() { return firstName; }
                @Override public String getCity() { return city; }
                @Override public String getUsername() { return null; }
                @Override public String getEmailAddress() { return null; }
                @Override public String getLastName() { return null; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getPhoneNumber() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public String getPassword() { return null; }
                @Override public String getRole() { return null; }
            };

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserResponse response = userService.updateUser(rawToken, 1L, request);

            // Then
            assertThat(response).isNotNull();
            verify(userRepository).save(argThat(user ->
                    user.getFirstName().equals("Updated") &&
                    user.getCity().equals("New York")
            ));
        }

        @Test
        @DisplayName("Should prevent non-admin from updating other users")
        void shouldPreventNonAdminFromUpdatingOthers() {
            // Given
            UserUpdateRequest request = new UserUpdateRequest() {
                {
                    setFirstName("Hacked");
                }

                private String firstName;

                public void setFirstName(String firstName) { this.firstName = firstName; }

                @Override public String getFirstName() { return firstName; }
                @Override public String getUsername() { return null; }
                @Override public String getEmailAddress() { return null; }
                @Override public String getLastName() { return null; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getPhoneNumber() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public String getPassword() { return null; }
                @Override public String getRole() { return null; }
            };

            User otherUser = User.builder()
                    .id(99L)
                    .username("otheruser")
                    .email("other@example.com")
                    .firstName("Other")
                    .lastName("User")
                    .role(UserRole.STUDENT)
                    .active(true)
                    .build();

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.findWithProfileById(99L)).thenReturn(Optional.of(otherUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(rawToken, 99L, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Not allowed to update this user")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow admin to update other users")
        void shouldAllowAdminToUpdateOthers() {
            // Given
            UserUpdateRequest request = new UserUpdateRequest() {
                {
                    setFirstName("AdminUpdated");
                }

                private String firstName;

                public void setFirstName(String firstName) { this.firstName = firstName; }

                @Override public String getFirstName() { return firstName; }
                @Override public String getUsername() { return null; }
                @Override public String getEmailAddress() { return null; }
                @Override public String getLastName() { return null; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getPhoneNumber() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public String getPassword() { return null; }
                @Override public String getRole() { return null; }
            };

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(adminAuthToken));
            when(userRepository.findWithProfileById(2L)).thenReturn(Optional.of(adminUser));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UserResponse response = userService.updateUser(adminToken, 1L, request);

            // Then
            assertThat(response).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should prevent username changes")
        void shouldPreventUsernameChange() {
            // Given
            UserUpdateRequest request = new UserUpdateRequest() {
                {
                    setUsername("newusername");
                }

                private String username;

                public void setUsername(String username) { this.username = username; }

                @Override public String getUsername() { return username; }
                @Override public String getEmailAddress() { return null; }
                @Override public String getFirstName() { return null; }
                @Override public String getLastName() { return null; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getPhoneNumber() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public String getPassword() { return null; }
                @Override public String getRole() { return null; }
            };

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(rawToken, 1L, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Username cannot be changed")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should prevent non-admin from changing role")
        void shouldPreventNonAdminFromChangingRole() {
            // Given
            UserUpdateRequest request = new UserUpdateRequest() {
                {
                    setRole("ADMIN");
                }

                private String role;

                public void setRole(String role) { this.role = role; }

                @Override public String getRole() { return role; }
                @Override public String getUsername() { return null; }
                @Override public String getEmailAddress() { return null; }
                @Override public String getFirstName() { return null; }
                @Override public String getLastName() { return null; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getPhoneNumber() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public String getPassword() { return null; }
            };

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(rawToken, 1L, request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Super admin privileges required to change role")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Should allow password update")
        void shouldAllowPasswordUpdate() {
            // Given
            UserUpdateRequest request = new UserUpdateRequest() {
                {
                    setPassword("newPassword123");
                }

                private String password;

                public void setPassword(String password) { this.password = password; }

                @Override public String getPassword() { return password; }
                @Override public String getUsername() { return null; }
                @Override public String getEmailAddress() { return null; }
                @Override public String getFirstName() { return null; }
                @Override public String getLastName() { return null; }
                @Override public String getEmailVisibility() { return null; }
                @Override public String getCity() { return null; }
                @Override public String getCountry() { return null; }
                @Override public String getTimezone() { return null; }
                @Override public String getDescription() { return null; }
                @Override public String getInterest() { return null; }
                @Override public String getPhoneNumber() { return null; }
                @Override public String getAvatarUrl() { return null; }
                @Override public String getRole() { return null; }
            };

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncodedHash");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.updateUser(rawToken, 1L, request);

            // Then
            verify(passwordEncoder).encode("newPassword123");
            verify(userRepository).save(argThat(user ->
                    user.getPasswordHash().equals("$2a$10$newEncodedHash")
            ));
        }
    }

    @Nested
    @DisplayName("Lock/Unlock User Tests")
    class LockUnlockTests {

        private String adminToken;
        private AuthToken adminAuthToken;

        @BeforeEach
        void setUp() {
            adminToken = UUID.randomUUID().toString();
            adminAuthToken = AuthToken.builder()
                    .id(2L)
                    .user(adminUser)
                    .tokenHash("adminTokenHash")
                    .tokenJti(UUID.randomUUID())
                    .type(TokenType.ACCESS)
                    .revoked(false)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();
        }

        @Test
        @DisplayName("Should allow admin to lock user")
        void shouldAllowAdminToLockUser() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(adminAuthToken));
            when(userRepository.findWithProfileById(2L)).thenReturn(Optional.of(adminUser));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.lockUser(adminToken, 1L);

            // Then
            verify(userRepository).save(argThat(user -> !user.isActive()));
        }

        @Test
        @DisplayName("Should allow admin to unlock user")
        void shouldAllowAdminToUnlockUser() {
            // Given
            testUser.setActive(false);
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(adminAuthToken));
            when(userRepository.findWithProfileById(2L)).thenReturn(Optional.of(adminUser));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.unlockUser(adminToken, 1L);

            // Then
            verify(userRepository).save(argThat(User::isActive));
        }

        @Test
        @DisplayName("Should prevent non-admin from locking user")
        void shouldPreventNonAdminFromLocking() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.lockUser(rawToken, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Admin only")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prevent non-admin from unlocking user")
        void shouldPreventNonAdminFromUnlocking() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.unlockUser(rawToken, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Admin only")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        private String adminToken;
        private AuthToken adminAuthToken;

        @BeforeEach
        void setUp() {
            adminToken = UUID.randomUUID().toString();
            adminAuthToken = AuthToken.builder()
                    .id(2L)
                    .user(adminUser)
                    .tokenHash("adminTokenHash")
                    .tokenJti(UUID.randomUUID())
                    .type(TokenType.ACCESS)
                    .revoked(false)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();
        }

        @Test
        @DisplayName("Should allow admin to delete user")
        void shouldAllowAdminToDeleteUser() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(adminAuthToken));
            when(userRepository.findWithProfileById(2L)).thenReturn(Optional.of(adminUser));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When
            userService.deleteUser(adminToken, 1L);

            // Then
            verify(authTokenRepository).deleteAllByUser(testUser);
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("Should prevent deleting admin user")
        void shouldPreventDeletingAdmin() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(adminAuthToken));
            when(userRepository.findWithProfileById(2L)).thenReturn(Optional.of(adminUser));

            User anotherAdmin = User.builder()
                    .id(3L)
                    .username("admin2")
                    .role(UserRole.ADMIN)
                    .admin(true)
                    .build();

            when(userRepository.findWithProfileById(3L)).thenReturn(Optional.of(anotherAdmin));

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(adminToken, 3L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Cannot delete admin user")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should prevent non-admin from deleting user")
        void shouldPreventNonAdminFromDeleting() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(rawToken, 1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Admin privileges required")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate active token successfully")
        void shouldValidateActiveToken() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));

            // When
            TokenStatusResponse response = userService.checkToken(rawToken);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.isValid()).isTrue();
            assertThat(response.isAdmin()).isFalse();
            assertThat(response.getExpiresAt()).isEqualTo(validToken.getExpiresAt());
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Given
            validToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));

            // When & Then
            assertThatThrownBy(() -> userService.checkToken(rawToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Invalid or expired token")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should reject revoked token")
        void shouldRejectRevokedToken() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.checkToken(rawToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Invalid or expired token")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // When & Then
            assertThatThrownBy(() -> userService.checkToken(null))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Missing authentication token")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should reject blank token")
        void shouldRejectBlankToken() {
            // When & Then
            assertThatThrownBy(() -> userService.checkToken("   "))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Missing authentication token")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should reject token for inactive user")
        void shouldRejectTokenForInactiveUser() {
            // Given
            testUser.setActive(false);
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));

            // When & Then
            assertThatThrownBy(() -> userService.checkToken(rawToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("User account is inactive")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully and revoke token")
        void shouldLogoutSuccessfully() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(authTokenRepository.save(any(AuthToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.logout(rawToken);

            // Then
            verify(authTokenRepository).save(argThat(AuthToken::isRevoked));
        }

        @Test
        @DisplayName("Should fail logout with invalid token")
        void shouldFailLogoutWithInvalidToken() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.logout(rawToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Invalid or expired token")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("Get All Students Tests")
    class GetAllStudentsTests {

        private String adminToken;
        private AuthToken adminAuthToken;

        @BeforeEach
        void setUp() {
            adminToken = UUID.randomUUID().toString();
            adminAuthToken = AuthToken.builder()
                    .id(2L)
                    .user(adminUser)
                    .tokenHash("adminTokenHash")
                    .tokenJti(UUID.randomUUID())
                    .type(TokenType.ACCESS)
                    .revoked(false)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();
        }

        @Test
        @DisplayName("Should allow admin to get all students")
        void shouldAllowAdminToGetAllStudents() {
            // Given
            User student1 = User.builder()
                    .id(10L)
                    .legacyUserId(10L)
                    .username("student1")
                    .email("student1@example.com")
                    .firstName("Student")
                    .lastName("One")
                    .role(UserRole.STUDENT)
                    .active(true)
                    .build();

            User student2 = User.builder()
                    .id(11L)
                    .legacyUserId(11L)
                    .username("student2")
                    .email("student2@example.com")
                    .firstName("Student")
                    .lastName("Two")
                    .role(UserRole.STUDENT)
                    .active(true)
                    .build();

            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(adminAuthToken));
            when(userRepository.findWithProfileById(2L)).thenReturn(Optional.of(adminUser));
            when(userRepository.findAllStudents()).thenReturn(Arrays.asList(student1, student2));

            // When
            List<UserResponse> students = userService.getAllStudents(adminToken);

            // Then
            assertThat(students).hasSize(2);
            assertThat(students).extracting(UserResponse::getUsername)
                    .containsExactlyInAnyOrder("student1", "student2");
        }

        @Test
        @DisplayName("Should prevent non-admin from getting all students")
        void shouldPreventNonAdminFromGettingAllStudents() {
            // Given
            when(authTokenRepository.findByTokenHashAndRevokedFalse(any())).thenReturn(Optional.of(validToken));
            when(userRepository.findWithProfileById(1L)).thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.getAllStudents(rawToken))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Admin privileges required")
                    .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);

            verify(userRepository, never()).findAllStudents();
        }
    }
}
