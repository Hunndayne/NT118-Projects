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
	List<Lesson> findByClazz_IdIn(Iterable<Long> classIds);
	void deleteByClazz_IdIn(Iterable<Long> classIds);
	long countByClazz_Course_Id(Long courseId);
	long countByClazz_IdIn(Iterable<Long> classIds);
}
