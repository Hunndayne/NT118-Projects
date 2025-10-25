package com.finalproject.backend.repository;

import com.finalproject.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByUsername(String username);

    boolean existsByEmailAddress(String emailAddress);

    Optional<User> findByUsername(String username);
}
