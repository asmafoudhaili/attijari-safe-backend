package com.example.backend.Controller;


import com.example.backend.entity.Notification;
import com.example.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/notifications")
    public String storeNotification(@RequestBody Notification notification) {
        notificationService.saveNotification(notification);
        return "{\"status\": \"success\"}";
    }

    @GetMapping("/unsafe-alerts")
    public List<Notification> getUnsafeAlerts() {
        return notificationService.getUnsafeNotifications();
    }
}
