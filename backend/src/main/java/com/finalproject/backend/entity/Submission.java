package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "submission_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id")
	private Assignment assignment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	private User student;

	@Column(name = "attempt_no")
	private Integer attemptNo;

	@Column(name = "submitted_at")
	private OffsetDateTime submittedAt;

	@Column(name = "content", columnDefinition = "text")
	private String content;

	@Column(name = "file_url", columnDefinition = "text")
	private String fileUrl;

	@Column(name = "status", length = 24)
	private String status;

	@Column(name = "score")
	private BigDecimal score;

	@Column(name = "feedback", columnDefinition = "text")
	private String feedback;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade_by")
	private User gradedBy;

	@Column(name = "grade_at")
	private OffsetDateTime gradeAt;
}
