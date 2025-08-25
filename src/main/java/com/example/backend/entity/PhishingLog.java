// com.example.backend.entity.PhishingLog.java
package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phishing_log")
public class PhishingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "details", nullable = false)
    private String details;

    @Column(name = "is_safe", nullable = false)
    private boolean isSafe;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user", nullable = false)
    private String user;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean safe) { this.isSafe = safe; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}
