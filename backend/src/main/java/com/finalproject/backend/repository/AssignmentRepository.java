package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
	List<Assignment> findDistinctByClazz_Teachers_Id(Long teacherId);
	List<Assignment> findDistinctByClazz_Course_Students_Id(Long studentId);
	List<Assignment> findByClazz_IdOrderByDeadlineAsc(Long classId);
	java.util.Optional<Assignment> findByIdAndClazz_Id(Long assignmentId, Long classId);
}
