package com.example.backend.repository;

import com.example.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByDetailsHashAndThreatType(String detailsHash, String threatType);
    List<Notification> findByIsSafeFalseAndAdminConfirmedFalse();
}
