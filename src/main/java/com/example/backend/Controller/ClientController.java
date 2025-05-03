package com.example.backend.Controller;

import com.example.backend.entity.Log;
import com.example.backend.repository.LogRepository;
import com.example.backend.service.CodeSafetyService;
import com.example.backend.service.PhishingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final PhishingService phishingService;
    private final CodeSafetyService codeSafetyService;
    private final LogRepository logRepository;

    public ClientController(PhishingService phishingService,
                            CodeSafetyService codeSafetyService,
                            LogRepository logRepository) {
        this.phishingService = phishingService;
        this.codeSafetyService = codeSafetyService;
        this.logRepository = logRepository;
    }

    @PostMapping("/phishing")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    public ResponseEntity<Map<String, Boolean>> checkPhishing(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        boolean isPhishing = phishingService.checkPhishing(url);
        logRepository.save(new Log());
        return ResponseEntity.ok(Map.of("isPhishing", isPhishing));
    }

    @PostMapping("/code-safety")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    public ResponseEntity<Map<String, Object>> checkCodeSafety(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        boolean isSafe = codeSafetyService.checkCodeSafety(code);
        int positives = codeSafetyService.getPositives(code);
        logRepository.save(new Log());
        return ResponseEntity.ok(Map.of("isSafe", isSafe, "positives", positives));
    }
}
