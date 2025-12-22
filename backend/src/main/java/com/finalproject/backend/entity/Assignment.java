package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "assignment_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_id")
	private ClassEntity clazz;

	@Column(name = "title", length = 255)
	private String title;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "attachment_url", columnDefinition = "text")
	private String attachmentUrl;

	@Column(name = "deadline")
	private OffsetDateTime deadline;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "weight")
	private BigDecimal weight;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
