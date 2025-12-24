package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
	List<Submission> findByAssignment_Id(Long assignmentId);
	Optional<Submission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
}
