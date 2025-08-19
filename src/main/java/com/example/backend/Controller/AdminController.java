package com.example.backend.controller;

import com.example.backend.entity.Log;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.LogRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Matches plain 'ADMIN' role from JWT
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogRepository logRepository;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder, LogRepository logRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.logRepository = logRepository;
    }

    // ✅ Create user (CLIENT or EMPLOYEE)
    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
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

    // ✅ Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return userRepository.findAll();
    }

    // ✅ Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
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

    // ✅ Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        if (userRepository.existsById(id)) {
            logRepository.save(new Log());
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ✅ Get logs
    @GetMapping("/logs")
    public ResponseEntity<List<Log>> getLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + auth.getName() + ", Authorities: " + auth.getAuthorities());
        List<Log> logs = logRepository.findAll();
        return ResponseEntity.ok(logs);
    }
}
