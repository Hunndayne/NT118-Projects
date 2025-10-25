package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.LoginRequest;
import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.dto.response.LoginResponse;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.entity.UserToken;
import com.finalproject.backend.repository.UserRepository;
import com.finalproject.backend.repository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {
    private static final Duration TOKEN_TTL = Duration.ofDays(1);

    @Autowired
    private UserRepository userrepository;

    @Autowired
    private UserTokenRepository userTokenRepository;

    public User createUser(UserCreationRequest request) {
        String username = request.getUsername().trim();
        String emailAddress = request.getEmailAddress().trim();

        if (userrepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userrepository.existsByEmailAddress(emailAddress)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email address already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setFirstname(request.getFirstname().trim());
        user.setLastname(request.getLastname().trim());
        user.setEmailAddress(emailAddress);
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setTimezone(request.getTimezone());
        user.setPhoneNumber(request.getPhoneNumber().trim());

        return userrepository.save(user);
    }

    public User getUserByToken(String token) {
        UserToken userToken = userTokenRepository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
        return userToken.getUser();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        String rawPassword = request.getPassword();

        userTokenRepository.deleteByExpiresAtBefore(Instant.now());

        User user = userrepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getPassword().equals(rawPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(TOKEN_TTL);
        String tokenValue = UUID.randomUUID().toString();

        UserToken userToken = UserToken.builder()
                .token(tokenValue)
                .createdAt(now)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        userTokenRepository.save(userToken);

        return LoginResponse.builder()
                .token(tokenValue)
                .expiresAt(expiresAt)
                .build();
    }
}
