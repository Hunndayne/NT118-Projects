package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	boolean existsByCodeIgnoreCase(String code);

	Optional<Course> findByCodeIgnoreCase(String code);

	Optional<Course> findByIdAndStudents_Id(Long id, Long studentId);

	List<Course> findDistinctByStudents_Id(Long studentId);

	Optional<Course> findByIdAndTeachers_Id(Long id, Long teacherId);

	List<Course> findDistinctByTeachers_Id(Long teacherId);

	@Query("SELECT COUNT(DISTINCT s.id) FROM Course c JOIN c.students s WHERE c.id IN :courseIds")
	long countDistinctStudentsByCourseIds(@Param("courseIds") Set<Long> courseIds);
}
