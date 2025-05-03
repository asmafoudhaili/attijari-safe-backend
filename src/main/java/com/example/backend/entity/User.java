package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password; // Hashed

    @Enumerated(EnumType.STRING)
    private Role role;

    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String avatar; // Add avatar field

    // Constructors
    public User() {}

    public User(String username, String password, Role role, String mobileNumber, Gender gender, String avatar) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.mobileNumber = mobileNumber;
        this.gender = gender;
        this.avatar = avatar;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
