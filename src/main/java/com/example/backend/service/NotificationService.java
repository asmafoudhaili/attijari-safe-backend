package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    private final Sinks.Many<Notification> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    public boolean saveNotification(Notification notification) {
        Optional<Notification> existing = notificationRepository.findByDetailsHashAndThreatType(notification.getDetailsHash(), notification.getThreatType());
        if (existing.isPresent()) {
            return false; // Duplicate notification
        }
        notificationRepository.save(notification);
        return true;
    }

    public void broadcast(Notification notification) {
        notificationSink.tryEmitNext(notification);
    }

    public Flux<Notification> getNotificationStream() {
        return notificationSink.asFlux();
    }

    public List<Notification> getUnsafeNotifications() {
        return notificationRepository.findByIsSafeFalseAndAdminConfirmedFalse();
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
