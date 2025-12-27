package com.finalproject.backend.repository;

import com.finalproject.backend.entity.PasswordResetToken;
import com.finalproject.backend.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findFirstByUserAndOtpCodeAndUsedAtIsNullOrderByCreatedAtDesc(User user, String otpCode);
    long countByUserAndCreatedAtAfter(User user, Instant createdAfter);
}
