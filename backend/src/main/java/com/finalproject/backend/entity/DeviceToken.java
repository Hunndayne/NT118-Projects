package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "device_tokens", uniqueConstraints = {
		@UniqueConstraint(name = "uk_device_tokens_token", columnNames = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "device_token_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token", length = 512, nullable = false)
	private String token;

	@Column(name = "platform", length = 32, nullable = false)
	private String platform;

	@Column(name = "device_id", length = 128)
	private String deviceId;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "last_seen")
	private Instant lastSeen;

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
		if (lastSeen == null) {
			lastSeen = Instant.now();
		}
		if (!active) {
			active = true;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
