package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.ForgotPasswordRequest;
import com.finalproject.backend.dto.request.ResetPasswordRequest;
import com.finalproject.backend.dto.response.MessageResponse;
import com.finalproject.backend.entity.PasswordResetToken;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.PasswordResetTokenRepository;
import com.finalproject.backend.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final int otpExpiryMinutes;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@enggo.local}") String mailFrom,
            @Value("${app.reset.otp-exp-min:10}") int otpExpiryMinutes
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.otpExpiryMinutes = otpExpiryMinutes;
    }

    public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        String email = trimRequired(request.getEmail(), "email");
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return new MessageResponse("If the email exists, an OTP has been sent.");
        }

        User user = userOpt.get();
        String otp = generateOtp(4);
        Instant now = Instant.now();

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setOtpCode(otp);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(otpExpiryMinutes, ChronoUnit.MINUTES));
        passwordResetTokenRepository.save(token);

        sendOtpEmail(email, otp, otpExpiryMinutes);
        return new MessageResponse("OTP sent to email.");
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = trimRequired(request.getEmail(), "email");
        String otp = trimRequired(request.getOtp(), "otp");
        String newPassword = trimRequired(request.getNewPassword(), "newPassword");

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));

        PasswordResetToken token = passwordResetTokenRepository
                .findFirstByUserAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDesc(user, otp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        return new MessageResponse("Password updated.");
    }

    private void sendOtpEmail(String email, String otp, int expiryMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(mailFrom);
        message.setSubject("EngGo password reset OTP");
        message.setText("Your OTP is: " + otp + "\nThis code expires in " + expiryMinutes + " minutes.");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Email send failed");
        }
    }

    private String trimRequired(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value.trim();
    }

    private String generateOtp(int digits) {
        int bound = (int) Math.pow(10, digits);
        int number = RANDOM.nextInt(bound);
        return String.format("%0" + digits + "d", number);
    }
}
