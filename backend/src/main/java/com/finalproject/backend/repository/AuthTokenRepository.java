package com.finalproject.backend.repository;

import com.finalproject.backend.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

	Optional<AuthToken> findByTokenHashAndRevokedFalse(String tokenHash);

	void deleteByExpiresAtBefore(Instant instant);
}
