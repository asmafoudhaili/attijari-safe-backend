package com.example.backend.service;

import com.example.backend.entity.Log;
import com.example.backend.repository.LogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class CodeSafetyService {

    @Value("${virustotal.api.key}")
    private String virusTotalApiKey;

    private final LogRepository logRepository;
    private final Map<String, Boolean> codeCache = new HashMap<>();

    public CodeSafetyService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    // Helper function to generate a simple hash
    private String simpleHash(String str) {
        int hash = 0;
        for (char c : str.toCharArray()) {
            hash = (hash << 5) - hash + c;
            hash |= 0;
        }
        return String.valueOf(hash);
    }

    public boolean checkCodeSafety(String code) {
        String codeHash = simpleHash(code);

        // Check cache
        if (codeCache.containsKey(codeHash)) {
            return codeCache.get(codeHash);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Upload the code to VirusTotal
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apikey", virusTotalApiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.ByteArrayResource(code.getBytes()) {
                @Override
                public String getFilename() {
                    return "code.txt";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            // Use ParameterizedTypeReference to specify the exact type
            ResponseEntity<Map<String, Object>> uploadResponse = restTemplate.exchange(
                    "https://www.virustotal.com/api/v3/files",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            String analysisUrl;
            // Safely navigate the response structure
            Map<String, Object> uploadBody = uploadResponse.getBody();
            if (uploadBody == null || !uploadBody.containsKey("data")) {
                throw new RuntimeException("Invalid VirusTotal upload response: 'data' field missing");
            }

            Object dataObj = uploadBody.get("data");
            if (!(dataObj instanceof Map)) {
                throw new RuntimeException("Invalid VirusTotal upload response: 'data' is not a map");
            }

            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            if (!dataMap.containsKey("links")) {
                throw new RuntimeException("Invalid VirusTotal upload response: 'links' field missing");
            }

            Object linksObj = dataMap.get("links");
            if (!(linksObj instanceof Map)) {
                throw new RuntimeException("Invalid VirusTotal upload response: 'links' is not a map");
            }

            Map<String, Object> linksMap = (Map<String, Object>) linksObj;
            if (!linksMap.containsKey("self")) {
                throw new RuntimeException("Invalid VirusTotal upload response: 'self' field missing");
            }

            analysisUrl = (String) linksMap.get("self");

            // Poll for analysis results
            int attempts = 0;
            int maxAttempts = 10;
            int pollInterval = 3000;

            while (attempts < maxAttempts) {
                // Use ParameterizedTypeReference to specify the exact type
                ResponseEntity<Map<String, Object>> analysisResponse = restTemplate.exchange(
                        analysisUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );

                Map<String, Object> analysisBody = analysisResponse.getBody();
                if (analysisBody == null || !analysisBody.containsKey("data")) {
                    throw new RuntimeException("Invalid VirusTotal analysis response: 'data' field missing");
                }

                Object analysisDataObj = analysisBody.get("data");
                if (!(analysisDataObj instanceof Map)) {
                    throw new RuntimeException("Invalid VirusTotal analysis response: 'data' is not a map");
                }

                Map<String, Object> analysisDataMap = (Map<String, Object>) analysisDataObj;
                if (!analysisDataMap.containsKey("attributes")) {
                    throw new RuntimeException("Invalid VirusTotal analysis response: 'attributes' field missing");
                }

                Object attributesObj = analysisDataMap.get("attributes");
                if (!(attributesObj instanceof Map)) {
                    throw new RuntimeException("Invalid VirusTotal analysis response: 'attributes' is not a map");
                }

                Map<String, Object> attributesMap = (Map<String, Object>) attributesObj;

                if (!attributesMap.containsKey("status")) {
                    throw new RuntimeException("Invalid VirusTotal analysis response: 'status' field missing");
                }
                String status = (String) attributesMap.get("status");

                if (status.equals("completed")) {
                    if (!attributesMap.containsKey("last_analysis_stats")) {
                        throw new RuntimeException("Invalid VirusTotal analysis response: 'last_analysis_stats' field missing");
                    }

                    Object statsObj = attributesMap.get("last_analysis_stats");
                    if (!(statsObj instanceof Map)) {
                        throw new RuntimeException("Invalid VirusTotal analysis response: 'last_analysis_stats' is not a map");
                    }

                    Map<String, Object> statsMap = (Map<String, Object>) statsObj;
                    if (!statsMap.containsKey("malicious")) {
                        throw new RuntimeException("Invalid VirusTotal analysis response: 'malicious' field missing");
                    }

                    int maliciousCount = (int) statsMap.get("malicious");
                    boolean isSafe = maliciousCount == 0;
                    codeCache.put(codeHash, isSafe);

                    // Log the scan
                    Log log = new Log();
                    log.setUrl(code); // Using "url" field to store code
                    log.setType("CodeSafety");
                    log.setIsSafe(isSafe);
                    log.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    logRepository.save(log);

                    return isSafe;
                } else if (status.equals("queued") || status.equals("running")) {
                    Thread.sleep(pollInterval);
                    attempts++;
                } else {
                    return false;
                }
            }

            codeCache.put(codeHash, false);
            return false;
        } catch (Exception e) {
            System.err.println("VirusTotal API error: " + e.getMessage());
            codeCache.put(codeHash, false);
            return false;
        }
    }
}
