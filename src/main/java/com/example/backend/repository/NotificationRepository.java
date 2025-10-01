package com.example.backend.repository;

import com.example.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByIsSafeFalseAndAdminConfirmedFalse();
    List<Notification> findByDetailsHashAndThreatType(String detailsHash, String threatType);
}
