package com.finalproject.backend.repository;

import com.finalproject.backend.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, String> {
    Optional<UserToken> findByToken(String token);

    void deleteByExpiresAtBefore(Instant instant);
}
