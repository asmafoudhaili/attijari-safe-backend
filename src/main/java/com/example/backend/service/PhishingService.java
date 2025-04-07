package com.example.backend.service;

import com.example.backend.entity.Log;
import com.example.backend.repository.LogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class PhishingService {

    @Value("${google.safebrowsing.api.key}")
    private String googleSafeBrowsingApiKey;

    private final LogRepository logRepository;
    private final Map<String, Boolean> phishingCache = new HashMap<>();

    public PhishingService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public boolean checkPhishing(String url) {
        // Check cache
        if (phishingCache.containsKey(url)) {
            return phishingCache.get(url);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + googleSafeBrowsingApiKey;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("client", Map.of("clientId", "CyberGuard", "clientVersion", "1.0"));
            requestBody.put("threatInfo", Map.of(
                    "threatTypes", new String[]{"MALWARE", "SOCIAL_ENGINEERING"},
                    "platformTypes", new String[]{"ANY_PLATFORM"},
                    "threatEntryTypes", new String[]{"URL"},
                    "threatEntries", new Map[]{Map.of("url", url)}
            ));

            Map<String, Object> response = restTemplate.postForObject(apiUrl, requestBody, Map.class);
            boolean isPhishing = response != null && response.containsKey("matches");

            // Cache the result
            phishingCache.put(url, isPhishing);

            // Log the scan
            Log log = new Log();
            log.setUrl(url);
            log.setType("Phishing");
            log.setIsSafe(!isPhishing); // true if safe (not phishing)
            log.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logRepository.save(log);

            return isPhishing;
        } catch (Exception e) {
            System.err.println("Safe Browsing API error: " + e.getMessage());
            return false;
        }
    }
}
