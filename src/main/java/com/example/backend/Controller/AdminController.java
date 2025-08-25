// com.example.backend.controller.AdminController.java
package com.example.backend.Controller;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogRepository logRepository;
    private final PhishingLogRepository phishingLogRepository;
    private final RansomwareLogRepository ransomwareLogRepository;
    private final DoSLogRepository doSLogRepository;
    private final CodeSafetyLogRepository codeSafetyLogRepository;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           LogRepository logRepository, PhishingLogRepository phishingLogRepository,
                           RansomwareLogRepository ransomwareLogRepository,
                           DoSLogRepository doSLogRepository,
                           CodeSafetyLogRepository codeSafetyLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.logRepository = logRepository;
        this.phishingLogRepository = phishingLogRepository;
        this.ransomwareLogRepository = ransomwareLogRepository;
        this.doSLogRepository = doSLogRepository;
        this.codeSafetyLogRepository = codeSafetyLogRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("createUser - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot create another admin");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        logRepository.save(new Log());
        return ResponseEntity.ok("User created successfully");
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getAllUsers - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getUserById - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("updateUser - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(updatedUser.getUsername());
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            user.setRole(updatedUser.getRole());
            user.setMobileNumber(updatedUser.getMobileNumber());
            user.setGender(updatedUser.getGender());
            user.setAvatar(updatedUser.getAvatar());
            logRepository.save(new Log());
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("deleteUser - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (userRepository.existsById(id)) {
            logRepository.save(new Log());
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Log>> getLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getLogs - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        List<Log> logs = logRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/phishing")
    public ResponseEntity<List<PhishingLog>> getPhishingLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getPhishingLogs - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return ResponseEntity.ok(phishingLogRepository.findAll());
    }

    @PostMapping("/logs/phishing")
    public ResponseEntity<PhishingLog> createPhishingLog(@RequestBody PhishingLog log) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("createPhishingLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(phishingLogRepository.save(log));
    }

    @GetMapping("/logs/ransomware")
    public ResponseEntity<List<RansomwareLog>> getRansomwareLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getRansomwareLogs - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return ResponseEntity.ok(ransomwareLogRepository.findAll());
    }

    @PostMapping("/logs/ransomware")
    public ResponseEntity<RansomwareLog> createRansomwareLog(@RequestBody RansomwareLog log) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("createRansomwareLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ransomwareLogRepository.save(log));
    }

    @GetMapping("/logs/dos")
    public ResponseEntity<List<DoSLog>> getDoSLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getDoSLogs - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return ResponseEntity.ok(doSLogRepository.findAll());
    }

    @PostMapping("/logs/dos")
    public ResponseEntity<DoSLog> createDoSLog(@RequestBody DoSLog log) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("createDoSLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(doSLogRepository.save(log));
    }

    @GetMapping("/logs/code-safety")
    public ResponseEntity<List<CodeSafetyLog>> getCodeSafetyLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("getCodeSafetyLogs - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return ResponseEntity.ok(codeSafetyLogRepository.findAll());
    }

    @PostMapping("/logs/code-safety")
    public ResponseEntity<CodeSafetyLog> createCodeSafetyLog(@RequestBody CodeSafetyLog log) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("createCodeSafetyLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(codeSafetyLogRepository.save(log));
    }

    @DeleteMapping("/logs/phishing/{id}")
    public ResponseEntity<Void> deletePhishingLog(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("deletePhishingLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (phishingLogRepository.existsById(id)) {
            phishingLogRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/logs/ransomware/{id}")
    public ResponseEntity<Void> deleteRansomwareLog(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("deleteRansomwareLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (ransomwareLogRepository.existsById(id)) {
            ransomwareLogRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/logs/dos/{id}")
    public ResponseEntity<Void> deleteDoSLog(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("deleteDoSLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (doSLogRepository.existsById(id)) {
            doSLogRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/logs/code-safety/{id}")
    public ResponseEntity<Void> deleteCodeSafetyLog(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("deleteCodeSafetyLog - Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (codeSafetyLogRepository.existsById(id)) {
            codeSafetyLogRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
