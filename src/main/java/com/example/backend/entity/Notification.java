package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String threatType;
    private String details;
    private String detailsHash;
    private String user;
    private String timestamp;
    private boolean isSafe;
    private boolean adminConfirmed;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getDetailsHash() { return detailsHash; }
    public void setDetailsHash(String detailsHash) { this.detailsHash = detailsHash; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean isSafe) { this.isSafe = isSafe; }
    public boolean isAdminConfirmed() { return adminConfirmed; }
    public void setAdminConfirmed(boolean adminConfirmed) { this.adminConfirmed = adminConfirmed; }
}
