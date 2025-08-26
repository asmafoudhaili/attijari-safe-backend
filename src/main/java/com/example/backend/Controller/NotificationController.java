package com.example.backend.Controller;

import com.example.backend.entity.Notification;
import com.example.backend.entity.Reclamation;
import com.example.backend.entity.SafeItem;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.ReclamationRepository;
import com.example.backend.repository.SafeItemRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
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
    private NotificationRepository notificationRepository;

    @Autowired
    private SafeItemRepository safeItemRepository;

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Store notification for admin
    @PostMapping("/admin/notifications")
    @PreAuthorize("hasRole('ADMIN')")
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

    // Store notification for client or admin
    @PostMapping("/notifications")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
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

    // Store reclamation from client or admin
    @PostMapping("/reclamations")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<?> submitReclamation(@RequestBody Reclamation reclamation, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            reclamation.setUser(username);
            reclamation.setProcessed(false);
            // Respect isSafe from the request payload
            reclamationRepository.save(reclamation);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Reclamation submitted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Get unprocessed reclamations for admin
    @GetMapping("/admin/reclamations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReclamations(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            List<Reclamation> reclamations = reclamationRepository.findByProcessedFalse();
            return ResponseEntity.ok(reclamations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Confirm reclamation as safe or not safe
    @PostMapping("/admin/confirm-reclamation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmReclamation(@RequestBody Reclamation updatedReclamation, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Optional<Reclamation> existing = reclamationRepository.findById(updatedReclamation.getId());
            if (!existing.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Reclamation not found"));
            }

            Reclamation reclamation = existing.get();
            reclamation.setProcessed(true);
            reclamation.setSafe(updatedReclamation.isSafe());
            reclamationRepository.save(reclamation);

            if (updatedReclamation.isSafe()) {
                SafeItem safeItem = new SafeItem(reclamation.getItemHash(), reclamation.getThreatType(), true, true);
                Optional<SafeItem> existingItem = safeItemRepository.findByItemHashAndThreatType(safeItem.getItemHash(), safeItem.getThreatType());
                SafeItem itemToSave;
                if (existingItem.isPresent()) {
                    itemToSave = existingItem.get();
                    itemToSave.setSafe(true);
                    itemToSave.setAdminConfirmed(true);
                } else {
                    itemToSave = safeItem;
                }
                safeItemRepository.save(itemToSave);

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
                notification.setThreatType(reclamation.getThreatType());
                notification.setDetails(reclamation.getDetails());
                notification.setDetailsHash(reclamation.getItemHash());
                notification.setUser(jwtUtil.extractUsername(token));
                notification.setSafe(true);
                notification.setAdminConfirmed(true);
                notification.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new java.util.Date()));
                boolean saved = notificationService.saveNotification(notification);
                if (saved) {
                    notificationService.broadcast(notification);
                }
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Reclamation confirmed"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", "FastAPI error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Existing endpoint for backward compatibility
    @PostMapping("/admin/confirm-safety")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmSafety(@RequestBody SafeItem safeItem, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
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
            notification.setUser(jwtUtil.extractUsername(token));
            notification.setSafe(safeItem.isSafe());
            notification.setAdminConfirmed(true);
            notification.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new java.util.Date()));
            boolean saved = notificationService.saveNotification(notification);
            if (saved) {
                notificationService.broadcast(notification);
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Safety status confirmed"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", "FastAPI error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/unsafe-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Notification> getUnsafeAlerts() {
        return notificationService.getUnsafeNotifications();
    }

    @GetMapping(value = "/admin/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Notification> streamNotifications(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            return notificationService.getNotificationStream();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/admin/notifications/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Notification> getNotificationHistory() {
        return notificationService.getAllNotifications();
    }
}
