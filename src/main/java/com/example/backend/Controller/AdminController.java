package com.example.backend.Controller;

import com.example.backend.entity.Log;
import com.example.backend.entity.User;
import com.example.backend.repository.LogRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CodeSafetyService;
import com.example.backend.service.PhishingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkPhishing(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        boolean isPhishing = phishingService.checkPhishing(url);
        logRepository.save(new Log());
        return ResponseEntity.ok(Map.of("isPhishing", isPhishing));
    }

    @PostMapping("/code-safety")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkCodeSafety(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        boolean isSafe = codeSafetyService.checkCodeSafety(code);
        int positives = codeSafetyService.getPositives(code); // Assuming this method exists
        logRepository.save(new Log());
        return ResponseEntity.ok(Map.of("isSafe", isSafe, "positives", positives));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Log> getLogs() {
        return logRepository.findAll();
    }

    @GetMapping("/logs/phishing")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Log> getPhishingLogs() {
        return logRepository.findByType("Phishing");
    }

    @GetMapping("/logs/codesafety")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Log> getCodeSafetyLogs() {
        return logRepository.findByType("CodeSafety");
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public User createUser(@RequestBody User user) {
        logRepository.save(new Log());
        return userRepository.save(user);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(updatedUser.getUsername());
            user.setPassword(updatedUser.getPassword());
            user.setRole(updatedUser.getRole());
            user.setMobileNumber(updatedUser.getMobileNumber());
            logRepository.save(new Log());
            return ResponseEntity.ok(userRepository.save(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            User user = userRepository.findById(id).get();
            logRepository.save(new Log());
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
