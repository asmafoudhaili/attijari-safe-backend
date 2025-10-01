package com.example.backend;

import com.example.backend.entity.Gender;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;

/**
 * Factory class for creating test data
 */
public class TestDataFactory {

    public static User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setRole(role);
        user.setMobileNumber("1234567890");
        user.setGender(Gender.MALE);
        user.setAvatar("avatar.jpg");
        return user;
    }

    public static User createUser(String username, Role role, String mobileNumber, Gender gender, String avatar) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setRole(role);
        user.setMobileNumber(mobileNumber);
        user.setGender(gender);
        user.setAvatar(avatar);
        return user;
    }

    public static User createAdminUser() {
        return createUser("admin", Role.ADMIN);
    }

    public static User createClientUser() {
        return createUser("client", Role.CLIENT);
    }
}
