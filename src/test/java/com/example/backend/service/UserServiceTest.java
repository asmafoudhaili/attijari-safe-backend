package com.example.backend.service;

import com.example.backend.entity.Gender;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User updatedUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole(Role.ADMIN);
        testUser.setMobileNumber("1234567890");
        testUser.setGender(Gender.MALE);
        testUser.setAvatar("avatar.jpg");

        updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setPassword("newhashedpassword");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setMobileNumber("0987654321");
        updatedUser.setGender(Gender.FEMALE);
        updatedUser.setAvatar("newavatar.jpg");
    }

    @Test
    @DisplayName("Should return all users when getAllUsers is called")
    void shouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser, updatedUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testUser, updatedUser);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should return user when getUserById is called with valid ID")
    void shouldReturnUserWhenGetUserByIdWithValidId() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when getUserById is called with invalid ID")
    void shouldReturnEmptyWhenGetUserByIdWithInvalidId() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should create and return user when createUser is called")
    void shouldCreateAndReturnUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update and return user when updateUser is called with valid ID")
    void shouldUpdateAndReturnUserWhenUpdateUserWithValidId() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Optional<User> result = userService.updateUser(1L, updatedUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(updatedUser.getUsername());
        assertThat(result.get().getPassword()).isEqualTo(updatedUser.getPassword());
        assertThat(result.get().getRole()).isEqualTo(updatedUser.getRole());
        assertThat(result.get().getMobileNumber()).isEqualTo(updatedUser.getMobileNumber());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return empty when updateUser is called with invalid ID")
    void shouldReturnEmptyWhenUpdateUserWithInvalidId() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.updateUser(999L, updatedUser);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user when deleteUser is called")
    void shouldDeleteUser() {
        // Given
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle repository exception during user creation")
    void shouldHandleRepositoryExceptionDuringUserCreation() {
        // Given
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
        verify(userRepository).save(testUser);
    }
}
