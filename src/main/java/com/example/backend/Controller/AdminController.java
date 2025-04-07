package com.example.backend.Controller;

import com.example.backend.entity.Log;
import com.example.backend.repository.LogRepository;
import com.example.backend.service.CodeSafetyService;
import com.example.backend.service.PhishingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin") // Changed from /api to /api/admin
public class AdminController {

    private final PhishingService phishingService;
    private final CodeSafetyService codeSafetyService;
    private final LogRepository logRepository;

    public AdminController(PhishingService phishingService, CodeSafetyService codeSafetyService, LogRepository logRepository) {
        this.phishingService = phishingService;
        this.codeSafetyService = codeSafetyService;
        this.logRepository = logRepository;
    }

    @PostMapping("/phishing")
    public Map<String, Boolean> checkPhishing(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        boolean isPhishing = phishingService.checkPhishing(url);
        return Map.of("isPhishing", isPhishing);
    }

    @PostMapping("/code-safety")
    public Map<String, Boolean> checkCodeSafety(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        boolean isSafe = codeSafetyService.checkCodeSafety(code);
        return Map.of("isSafe", isSafe);
    }

    @GetMapping("/logs")
    public List<Log> getLogs() {
        return logRepository.findAll();
    }

    @GetMapping("/logs/phishing")
    public List<Log> getPhishingLogs() {
        return logRepository.findByType("Phishing");
    }

    @GetMapping("/logs/codesafety")
    public List<Log> getCodeSafetyLogs() {
        return logRepository.findByType("CodeSafety");
    }
}
