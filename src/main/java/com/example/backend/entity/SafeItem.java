package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "safe_items")
public class SafeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemHash;
    private String threatType;
    private boolean isSafe;
    private boolean adminConfirmed;

    // Constructors
    public SafeItem() {}

    public SafeItem(String itemHash, String threatType, boolean isSafe, boolean adminConfirmed) {
        this.itemHash = itemHash;
        this.threatType = threatType;
        this.isSafe = isSafe;
        this.adminConfirmed = adminConfirmed;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemHash() { return itemHash; }
    public void setItemHash(String itemHash) { this.itemHash = itemHash; }
    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean isSafe) { this.isSafe = isSafe; }
    public boolean isAdminConfirmed() { return adminConfirmed; }
    public void setAdminConfirmed(boolean adminConfirmed) { this.adminConfirmed = adminConfirmed; }
}
