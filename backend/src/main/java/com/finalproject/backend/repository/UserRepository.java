package com.finalproject.backend.repository;

import com.finalproject.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.username) = LOWER(:username)")
	boolean existsByUsernameIgnoreCase(@Param("username") String username);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
	boolean existsByEmailIgnoreCase(@Param("email") String email);

	boolean existsByPhone(String phone);

	boolean existsByPhoneNumber(String phoneNumber);

	@Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
	Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
	Optional<User> findWithProfileById(@Param("id") Long id);
}
