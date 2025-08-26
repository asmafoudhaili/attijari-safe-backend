package com.example.backend.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class Reclamation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String user;
    private String itemHash;
    private String threatType;
    private String details;
    private boolean processed;
    private boolean safe;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getItemHash() { return itemHash; }
    public void setItemHash(String itemHash) { this.itemHash = itemHash; }
    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public boolean isSafe() { return safe; }
    public void setSafe(boolean safe) { this.safe = safe; }
}
