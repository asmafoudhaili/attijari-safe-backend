package com.example.backend.Controller;


import com.example.backend.entity.Log;
import com.example.backend.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private LogRepository logRepository;

    @GetMapping("/logs")
    public List<Log> getLogs() {
        return logRepository.findAll();
    }

    @GetMapping("/logs/phishing")
    public List<Log> getPhishingLogs() {
        return logRepository.findByType("PHISHING");
    }

    @GetMapping("/logs/code")
    public List<Log> getCodeLogs() {
        return logRepository.findByType("CODE_SAFETY");
    }

    @PostMapping("/logs")
    public ResponseEntity<Log> addLog(@RequestBody Log log) {
        Log savedLog = logRepository.save(log);
        return ResponseEntity.ok(savedLog);
    }
}
