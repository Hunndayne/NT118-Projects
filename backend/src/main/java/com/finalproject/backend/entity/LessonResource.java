package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lesons_resources") // bảng trong DB bị đánh vần thiếu chữ s
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResource {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "resource_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lesson_id")
	private Lesson lesson;

	@Column(name = "type", length = 32)
	private String type;

	@Column(name = "title", length = 255)
	private String title;

	@Column(name = "content", columnDefinition = "text")
	private String content;

	@Column(name = "url", columnDefinition = "text")
	private String url;

	@Column(name = "file_path", columnDefinition = "text")
	private String filePath;

	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
