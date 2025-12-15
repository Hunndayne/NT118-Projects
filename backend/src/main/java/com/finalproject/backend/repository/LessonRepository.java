package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
	List<Lesson> findByClazz_IdOrderByOrderIndexAsc(Long classId);
	Optional<Lesson> findByIdAndClazz_Id(Long id, Long classId);
}
