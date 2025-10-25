package com.finalproject.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "id")
	private Long legacyUserId;

	@Column(nullable = false, columnDefinition = "citext")
	private String username;

	@Column(nullable = false, columnDefinition = "citext")
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "is_admin", nullable = false)
	private boolean admin = false;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Builder.Default
	private Set<AuthToken> tokens = new HashSet<>();

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
