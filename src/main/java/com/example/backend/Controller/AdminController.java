package com.example.backend.Controller;

import com.example.backend.entity.Log;
import com.example.backend.entity.User;
import com.example.backend.repository.LogRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CodeSafetyService;
import com.example.backend.service.PhishingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PhishingService phishingService;
    private final CodeSafetyService codeSafetyService;
    private final LogRepository logRepository;
    private final UserRepository userRepository;

    public AdminController(PhishingService phishingService,
                           CodeSafetyService codeSafetyService,
                           LogRepository logRepository,
                           UserRepository userRepository) {
        this.phishingService = phishingService;
        this.codeSafetyService = codeSafetyService;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
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

    // 🚀 CRUD for Users

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(updatedUser.getUsername());
            user.setPassword(updatedUser.getPassword());
            user.setRole(updatedUser.getRole());
            user.setMobileNumber(updatedUser.getMobileNumber());
            return ResponseEntity.ok(userRepository.save(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
