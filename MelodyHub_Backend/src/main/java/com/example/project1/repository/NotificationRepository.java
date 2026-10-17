package com.example.project1.repository;

import com.example.project1.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndSeenFalse(Long userId);

    List<Notification> findByUser_IdAndSeenFalse(Long userId);
}
