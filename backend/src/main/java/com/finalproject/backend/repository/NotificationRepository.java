package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByOrderByCreatedAtDesc();

    // Find all notifications for a specific user (broadcast + targeted)
    @Query("SELECT n FROM Notification n WHERE n.targetUser IS NULL OR n.targetUser.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsForUser(@Param("userId") Long userId);

    // Find all notifications sent by a user
    @Query("SELECT n FROM Notification n WHERE n.sender.id = :senderId ORDER BY n.createdAt DESC")
    List<Notification> findBySenderId(@Param("senderId") Long senderId);
}
