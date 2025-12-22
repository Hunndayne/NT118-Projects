package com.finalproject.backend.repository;

import com.finalproject.backend.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
	List<ClassEntity> findDistinctByTeachers_Id(Long teacherId);
	List<ClassEntity> findDistinctByCourse_IdIn(Iterable<Long> courseIds);
}
