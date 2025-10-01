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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.net.URL;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.google.common.hash.Hashing;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

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

    @PostMapping("/admin/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> storeNotificationAdmin(@RequestBody Notification notification, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            notification.setUser(username);
            boolean saved = notificationService.saveNotification(notification);
            if (!saved) {
                logger.warn("Duplicate notification for user: {}", username);
                return ResponseEntity.status(409).body(Map.of("error", "Duplicate notification"));
            }
            notificationService.broadcast(notification);
            logger.info("Notification saved and broadcasted for user: {}", username);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            logger.error("Error saving admin notification: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<?> storeNotification(@RequestBody Notification notification, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            notification.setUser(username);
            boolean saved = notificationService.saveNotification(notification);
            if (!saved) {
                logger.warn("Duplicate notification for user: {}", username);
                return ResponseEntity.status(409).body(Map.of("error", "Duplicate notification"));
            }
            notificationService.broadcast(notification);
            logger.info("Notification saved and broadcasted for user: {}", username);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            logger.error("Error saving notification: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reclamations")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Transactional
    public ResponseEntity<?> submitReclamation(@RequestBody Reclamation reclamation, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            reclamation.setUser(username);
            reclamation.setProcessed(false);

            // Compute itemHash consistently with FastAPI
            String detailsJson = reclamation.getDetails();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> details = mapper.readValue(detailsJson, Map.class);
            String url = details.get("url");
            if (url == null) {
                logger.warn("No URL found in reclamation details: {}", detailsJson);
                return ResponseEntity.status(400).body(Map.of("error", "URL missing in reclamation details"));
            }

            // Normalize URL: strip query and fragment
            URI uri = new URI(url);
            String path = uri.getPath() != null ? uri.getPath().replaceAll("/+$", "") : "";
            URI cleaned = new URI(uri.getScheme(), uri.getAuthority(), path, null, null);
            String normalizedUrl = cleaned.toString();
            String computedHash = Hashing.sha256()
                    .hashString("{\"url\":\"" + normalizedUrl + "\"}", StandardCharsets.UTF_8)
                    .toString();
            reclamation.setItemHash(computedHash);
            logger.info("Computed itemHash for URL {}: {}", normalizedUrl, computedHash);

            // Check for existing unprocessed reclamation
            Optional<Reclamation> existingReclamation = reclamationRepository.findByItemHashAndThreatTypeAndProcessedFalse(computedHash, reclamation.getThreatType());
            if (existingReclamation.isPresent()) {
                logger.warn("Duplicate unprocessed reclamation for itemHash: {}, threatType: {}", computedHash, reclamation.getThreatType());
                return ResponseEntity.status(409).body(Map.of("error", "Duplicate unprocessed reclamation"));
            }

            reclamationRepository.save(reclamation);
            logger.info("Reclamation saved for user: {}, id: {}", username, reclamation.getId());
            return ResponseEntity.ok(Map.of("status", "success", "message", "Reclamation submitted"));
        } catch (Exception e) {
            logger.error("Error saving reclamation: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/reclamations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReclamations(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            List<Reclamation> reclamations = reclamationRepository.findByProcessedFalse();
            logger.info("Fetched {} unprocessed reclamations for admin: {}", reclamations.size(), username);
            return ResponseEntity.ok(reclamations);
        } catch (Exception e) {
            logger.error("Error fetching reclamations: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/confirm-reclamation")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> confirmReclamation(@RequestBody Reclamation updatedReclamation, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            logger.info("Processing reclamation confirmation for id: {} by admin: {}", updatedReclamation.getId(), username);

            Optional<Reclamation> existing = reclamationRepository.findById(updatedReclamation.getId());
            if (!existing.isPresent()) {
                logger.error("Reclamation not found for id: {}", updatedReclamation.getId());
                return ResponseEntity.status(404).body(Map.of("error", "Reclamation not found"));
            }

            Reclamation reclamation = existing.get();
            reclamation.setProcessed(true);
            reclamation.setSafe(updatedReclamation.isSafe());
            try {
                reclamationRepository.save(reclamation);
                logger.info("Reclamation id: {} updated: processed={}, safe={}", reclamation.getId(), reclamation.isProcessed(), reclamation.isSafe());
            } catch (Exception e) {
                logger.error("Failed to update reclamation id: {}: {}", reclamation.getId(), e.getMessage(), e);
                throw e;
            }

            if (updatedReclamation.isSafe()) {
                // Compute normalized item_hash for MySQL
                String detailsJson = reclamation.getDetails();
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> details = mapper.readValue(detailsJson, Map.class);
                String url = details.get("url");
                if (url == null) {
                    logger.error("URL missing in reclamation details: {}", detailsJson);
                    return ResponseEntity.status(400).body(Map.of("error", "URL missing in reclamation details"));
                }
                // Normalize URL: strip query and fragment
                URI uri = new URI(url);
                String path = uri.getPath() != null ? uri.getPath().replaceAll("/+$", "") : "";
                URI cleaned = new URI(uri.getScheme(), uri.getAuthority(), path, null, null);
                String normalizedUrl = cleaned.toString();
                String computedHash = Hashing.sha256()
                        .hashString("{\"url\":\"" + normalizedUrl + "\"}", StandardCharsets.UTF_8)
                        .toString();
                logger.info("Computed item_hash for URL {}: {}", normalizedUrl, computedHash);

                // Save SafeItem to JPA
                Optional<SafeItem> existingItem = safeItemRepository.findByItemHashAndThreatType(computedHash, reclamation.getThreatType());
                SafeItem safeItem = existingItem.orElse(new SafeItem());
                safeItem.setItemHash(computedHash);
                safeItem.setThreatType(reclamation.getThreatType());
                safeItem.setSafe(true);
                safeItem.setAdminConfirmed(true);
                try {
                    SafeItem savedItem = safeItemRepository.save(safeItem);
                    logger.info("Saved SafeItem to JPA: id={}, item_hash={}, threat_type={}", savedItem.getId(), computedHash, reclamation.getThreatType());
                } catch (Exception e) {
                    logger.error("Failed to save SafeItem to JPA: item_hash={}, error={}", computedHash, e.getMessage(), e);
                    throw new RuntimeException("Failed to save SafeItem: " + e.getMessage(), e);
                }

                // Verify SafeItem was saved
                Optional<SafeItem> savedItemCheck = safeItemRepository.findByItemHashAndThreatType(computedHash, reclamation.getThreatType());
                if (!savedItemCheck.isPresent()) {
                    logger.error("SafeItem not found after save: item_hash={}, threat_type={}", computedHash, reclamation.getThreatType());
                    throw new RuntimeException("SafeItem not found after save");
                }

                // Sync with FastAPI
                Map<String, Object> fastApiPayload = new java.util.HashMap<>();
                fastApiPayload.put("item_hash", "{\"url\":\"" + normalizedUrl + "\"}");
                fastApiPayload.put("threat_type", reclamation.getThreatType());
                fastApiPayload.put("is_safe", true);
                fastApiPayload.put("admin_confirmed", true);
                logger.debug("FastAPI payload: {}", fastApiPayload);

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", authHeader);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(fastApiPayload, headers);

                int retries = 3;
                while (retries > 0) {
                    try {
                        ResponseEntity<String> response = restTemplate.postForEntity(
                                "http://localhost:8000/admin/confirm-safe",
                                request,
                                String.class
                        );
                        logger.info("FastAPI response: status={}, body={}", response.getStatusCode(), response.getBody());
                        break;
                    } catch (HttpClientErrorException e) {
                        retries--;
                        logger.warn("FastAPI sync failed (attempt {}): status={}, error={}", 4 - retries, e.getStatusCode(), e.getMessage());
                        if (retries == 0) {
                            logger.error("Failed to sync with FastAPI after retries: {}", e.getMessage(), e);
                            return ResponseEntity.status(500).body(Map.of("error", "Failed to sync with FastAPI: " + e.getMessage()));
                        }
                        Thread.sleep(1000);
                    }
                }

                // Create and broadcast notification
                Notification notification = new Notification();
                notification.setThreatType(reclamation.getThreatType());
                notification.setDetails(reclamation.getDetails());
                notification.setDetailsHash(computedHash);
                notification.setUser(username);
                notification.setSafe(true);
                notification.setAdminConfirmed(true);
                notification.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new java.util.Date()));
                try {
                    boolean saved = notificationService.saveNotification(notification);
                    if (saved) {
                        notificationService.broadcast(notification);
                        logger.info("Notification created and broadcasted for safe item: {}", computedHash);
                    } else {
                        logger.warn("Notification not saved (possible duplicate) for safe item: {}", computedHash);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to save or broadcast notification for safe item {}: {}", computedHash, e.getMessage());
                }
            }

            logger.info("Reclamation confirmation completed for id: {}", reclamation.getId());
            return ResponseEntity.ok(Map.of("status", "success", "message", "Reclamation confirmed"));
        } catch (Exception e) {
            logger.error("Error confirming reclamation id: {}: {}", updatedReclamation.getId(), e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/confirm-safety")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> confirmSafety(@RequestBody SafeItem safeItem, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            Optional<SafeItem> existingItem = safeItemRepository.findByItemHashAndThreatType(safeItem.getItemHash(), safeItem.getThreatType());
            SafeItem itemToSave = existingItem.orElse(new SafeItem());
            itemToSave.setItemHash(safeItem.getItemHash());
            itemToSave.setThreatType(safeItem.getThreatType());
            itemToSave.setSafe(safeItem.isSafe());
            itemToSave.setAdminConfirmed(true);
            SafeItem savedItem = safeItemRepository.save(itemToSave);
            logger.info("Saved SafeItem to JPA: id={}, item_hash={}, threat_type={}", savedItem.getId(), safeItem.getItemHash(), safeItem.getThreatType());

            Map<String, Object> fastApiPayload = new java.util.HashMap<>();
            fastApiPayload.put("item_hash", safeItem.getItemHash());
            fastApiPayload.put("threat_type", safeItem.getThreatType());
            fastApiPayload.put("is_safe", safeItem.isSafe());
            fastApiPayload.put("admin_confirmed", true);
            logger.debug("FastAPI payload for /admin/confirm-safety: {}", fastApiPayload);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authHeader);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(fastApiPayload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8000/admin/confirm-safe", request, String.class);
            logger.info("FastAPI response for /admin/confirm-safety: status={}, body={}", response.getStatusCode(), response.getBody());

            Notification notification = new Notification();
            notification.setThreatType(safeItem.getThreatType());
            notification.setDetails(safeItem.getItemHash());
            notification.setDetailsHash(safeItem.getItemHash());
            notification.setUser(username);
            notification.setSafe(safeItem.isSafe());
            notification.setAdminConfirmed(true);
            notification.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new java.util.Date()));
            try {
                boolean saved = notificationService.saveNotification(notification);
                if (saved) {
                    notificationService.broadcast(notification);
                    logger.info("Notification created and broadcasted for safe item: {}", safeItem.getItemHash());
                } else {
                    logger.warn("Notification not saved (possible duplicate) for safe item: {}", safeItem.getItemHash());
                }
            } catch (Exception e) {
                logger.warn("Failed to save or broadcast notification for safe item {}: {}", safeItem.getItemHash(), e.getMessage());
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "Safety status confirmed"));
        } catch (HttpClientErrorException e) {
            logger.error("FastAPI error in /admin/confirm-safety: status={}, error={}", e.getStatusCode(), e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", "FastAPI error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error in /admin/confirm-safety: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/unsafe-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Notification> getUnsafeAlerts() {
        logger.info("Fetching unsafe notifications");
        return notificationService.getUnsafeNotifications();
    }

    @GetMapping(value = "/admin/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Notification> streamNotifications(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            logger.info("Streaming notifications for admin");
            return notificationService.getNotificationStream();
        } catch (Exception e) {
            logger.error("Error streaming notifications: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/admin/notifications/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Notification> getNotificationHistory() {
        logger.info("Fetching notification history");
        return notificationService.getAllNotifications();
    }
}
