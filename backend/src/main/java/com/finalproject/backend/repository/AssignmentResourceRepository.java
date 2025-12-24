package com.finalproject.backend.repository;

import com.finalproject.backend.entity.AssignmentResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentResourceRepository extends JpaRepository<AssignmentResource, Long> {
    List<AssignmentResource> findByAssignment_Id(Long assignmentId);
    Optional<AssignmentResource> findByIdAndAssignment_Id(Long resourceId, Long assignmentId);
    void deleteByAssignment_Id(Long assignmentId);
}
