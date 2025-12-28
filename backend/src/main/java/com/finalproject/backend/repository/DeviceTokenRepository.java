package com.finalproject.backend.repository;

import com.finalproject.backend.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

	Optional<DeviceToken> findByToken(String token);

	List<DeviceToken> findAllByUser_IdAndActiveTrue(Long userId);

	List<DeviceToken> findAllByUser_IdInAndActiveTrue(Collection<Long> userIds);

	List<DeviceToken> findAllByActiveTrue();

	void deleteAllByUser_Id(Long userId);
}
