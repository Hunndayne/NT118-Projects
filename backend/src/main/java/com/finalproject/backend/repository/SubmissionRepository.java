package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
	List<Submission> findByAssignment_Id(Long assignmentId);
	Optional<Submission> findByIdAndAssignment_Id(Long id, Long assignmentId);
	Optional<Submission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
	
	@Query("SELECT s FROM Submission s " +
		   "JOIN s.assignment a " +
		   "JOIN a.clazz c " +
		   "JOIN c.teachers t " +
		   "WHERE t.id = :teacherId")
	List<Submission> findByTeacherId(@Param("teacherId") Long teacherId);
}
