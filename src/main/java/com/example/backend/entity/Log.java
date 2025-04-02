package com.example.backend.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String type; // "PHISHING" or "CODE_SAFETY"
    private boolean isSafe;
    private LocalDateTime timestamp;

    // Constructors
    public Log() {}
    public Log(String url, String type, boolean isSafe, LocalDateTime timestamp) {
        this.url = url;
        this.type = type;
        this.isSafe = isSafe;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean isSafe) { this.isSafe = isSafe; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
