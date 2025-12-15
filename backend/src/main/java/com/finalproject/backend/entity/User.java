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

	@Column(name = "email_address")
	private String emailAddress;

	@Column(name = "email_visibility")
	private String emailVisibility;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	private String city;

	private String country;

	private String timezone;

	@Column(length = 1000)
	private String description;

	private String interest;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "is_admin", nullable = false)
	private boolean admin = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, columnDefinition = "user_role")
	@Builder.Default
	private UserRole role = UserRole.STUDENT;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "profile_image_path")
	private String profileImagePath;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Builder.Default
	private Set<AuthToken> tokens = new HashSet<>();

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private UserProfile profile;

	@ManyToMany(mappedBy = "students")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Set<Course> enrolledCourses = new HashSet<>();

	public boolean isSuperAdmin() {
		return role != null && role.isSuperAdmin();
	}

	public boolean isTeacher() {
		return role != null && role.isTeacher();
	}

	public boolean isStudent() {
		return role != null && role.isStudent();
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
