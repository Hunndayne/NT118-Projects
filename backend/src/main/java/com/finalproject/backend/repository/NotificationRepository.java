package com.finalproject.backend.repository;

import com.finalproject.backend.entity.Notification;
<<<<<<< HEAD
=======
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

>>>>>>> 15646fcd7e4282cf39290213b2b470e2c7dd21be
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
<<<<<<< HEAD
    List<Notification> findAllByOrderByCreatedAtDesc();
=======
	List<Notification> findAllByOrderByCreatedAtDesc();
>>>>>>> 15646fcd7e4282cf39290213b2b470e2c7dd21be
}
