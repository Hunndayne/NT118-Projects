package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long id;

	@Column(name = "type", length = 32, nullable = false)
	private String type;

	@Column(name = "title", length = 255, nullable = false)
	private String title;

	@Column(name = "content", columnDefinition = "text")
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id")
	private User targetUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_class_id")
	private ClassEntity targetClass;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
