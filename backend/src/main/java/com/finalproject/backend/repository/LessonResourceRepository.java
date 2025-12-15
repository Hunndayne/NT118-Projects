package com.finalproject.backend.repository;

import com.finalproject.backend.entity.LessonResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {
	List<LessonResource> findByLesson_Id(Long lessonId);
	Optional<LessonResource> findByIdAndLesson_Id(Long id, Long lessonId);
}
