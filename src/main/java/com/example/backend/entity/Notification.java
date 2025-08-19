package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String threatType;
    @Column(columnDefinition = "TEXT")
    private String details;
    private String detailsHash;
    private boolean isSafe;
    private LocalDateTime timestamp;
    private String user;

    // Constructors
    public Notification() {}

    public Notification(String threatType, String details, boolean isSafe, LocalDateTime timestamp, String user) {
        this.threatType = threatType;
        this.details = details;
        this.detailsHash = Integer.toString(details.hashCode());
        this.isSafe = isSafe;
        this.timestamp = timestamp;
        this.user = user;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getDetailsHash() { return detailsHash; }
    public void setDetailsHash(String detailsHash) { this.detailsHash = detailsHash; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean isSafe) { this.isSafe = isSafe; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public boolean isUnsafe() { return !isSafe; }
}
