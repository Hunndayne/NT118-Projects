package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_id")
	private Long id;

	@Column(length = 64, unique = true)
	private String code;

	@Column(length = 255, nullable = false)
	private String name;

	@Column(length = 64)
	private String level;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "status", nullable = false, length = 32)
	@Convert(converter = CourseStatusConverter.class)
	@Builder.Default
	private Boolean active = Boolean.TRUE;

	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_day_of_week", length = 16)
	private DayOfWeek dayOfWeek;

	@Column(name = "schedule_start_time")
	private LocalTime startTime;

	@Column(name = "schedule_end_time")
	private LocalTime endTime;

	@Column(name = "schedule_start_date")
	private LocalDate startDate;

	@Column(name = "schedule_end_date")
	private LocalDate endDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(name = "created_at")
	private Instant createdAt;

	@ManyToMany
	@JoinTable(
			name = "courses_students",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "student_id")
	)
	@Builder.Default
	private Set<User> students = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "courses_teachers",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "teacher_id")
	)
	@Builder.Default
	private Set<User> teachers = new HashSet<>();

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
