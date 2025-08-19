package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.entity.SafeItem;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.SafeItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SafeItemRepository safeItemRepository;

    public NotificationService(NotificationRepository notificationRepository, SafeItemRepository safeItemRepository) {
        this.notificationRepository = notificationRepository;
        this.safeItemRepository = safeItemRepository;
    }

    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    public List<Notification> getUnsafeNotifications() {
        return notificationRepository.findByIsSafeFalse();
    }

    public boolean isItemSafe(String itemHash, String threatType) {
        return safeItemRepository.findByItemHashAndThreatType(itemHash, threatType)
                .map(SafeItem::isAdminConfirmed)
                .orElse(false);
    }
}
