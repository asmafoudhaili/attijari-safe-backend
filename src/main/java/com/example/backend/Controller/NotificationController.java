package com.example.backend.Controller;

import com.example.backend.entity.Notification;
import com.example.backend.entity.SafeItem;
import com.example.backend.repository.SafeItemRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SafeItemRepository safeItemRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Existing admin-only endpoint
    @PostMapping("/admin/notifications")
    public ResponseEntity<?> storeNotificationAdmin(@RequestBody Notification notification, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            notification.setUser(username);
            boolean saved = notificationService.saveNotification(notification);
            if (!saved) {
                return ResponseEntity.status(409).body(Map.of("error", "Duplicate notification"));
            }
            notificationService.broadcast(notification);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint for CLIENT and ADMIN roles
    @PostMapping("/notifications")
    public ResponseEntity<?> storeNotification(@RequestBody Notification notification, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            notification.setUser(username);
            boolean saved = notificationService.saveNotification(notification);
            if (!saved) {
                return ResponseEntity.status(409).body(Map.of("error", "Duplicate notification"));
            }
            notificationService.broadcast(notification);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // New endpoint for admin to confirm safety
    @PostMapping("/admin/confirm-safety")
    public ResponseEntity<?> confirmSafety(@RequestBody SafeItem safeItem, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            if (!jwtUtil.extractRoles(token).contains("ADMIN")) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
            }

            Optional<SafeItem> existingItem = safeItemRepository.findByItemHashAndThreatType(safeItem.getItemHash(), safeItem.getThreatType());
            SafeItem itemToSave;
            if (existingItem.isPresent()) {
                itemToSave = existingItem.get();
                itemToSave.setSafe(safeItem.isSafe());
                itemToSave.setAdminConfirmed(true);
            } else {
                itemToSave = new SafeItem(safeItem.getItemHash(), safeItem.getThreatType(), safeItem.isSafe(), true);
            }
            safeItemRepository.save(itemToSave);

            // Notify FastAPI
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authHeader);
            HttpEntity<SafeItem> request = new HttpEntity<>(safeItem, headers);
            ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8000/admin/confirm-safe", request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to notify FastAPI"));
            }

            Notification notification = new Notification();
            notification.setThreatType(safeItem.getThreatType());
            notification.setDetails(safeItem.getItemHash());
            notification.setDetailsHash(safeItem.getItemHash());
            notification.setUser(username);
            notification.setSafe(safeItem.isSafe());
            notification.setAdminConfirmed(true);
            notificationService.broadcast(notification);

            return ResponseEntity.ok(Map.of("status", "success", "message", "Safety status confirmed"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/unsafe-alerts")
    public List<Notification> getUnsafeAlerts() {
        return notificationService.getUnsafeNotifications();
    }

    @GetMapping(value = "/admin/not america/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Notification> streamNotifications() {
        return notificationService.getNotificationStream();
    }

    @GetMapping("/admin/notifications/history")
    public List<Notification> getNotificationHistory() {
        return notificationService.getAllNotifications();
    }
}
