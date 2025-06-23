package com.promptdex.api.service;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.UserMapper;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserDetails mockUserDetails;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private User userToFollow;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setUsername("testUser");
        currentUser.setFollowing(new HashSet<>());

        userToFollow = new User();
        userToFollow.setId(UUID.randomUUID());
        userToFollow.setUsername("userToFollow");
        userToFollow.setFollowers(new HashSet<>());

        // --- FIX: The mock setup is REMOVED from the global @BeforeEach ---
    }

    @Test
    void followUser_shouldSuccessfullyFollowUser() {
        // Arrange
        // --- FIX: The mock setup is MOVED here, where it is actually needed. ---
        when(mockUserDetails.getUsername()).thenReturn(currentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));

        // Act
        userService.followUser("userToFollow", mockUserDetails);

        // Assert
        verify(userRepository, times(1)).save(currentUser);
        verify(userMapper, times(1)).toProfileDto(any(User.class), any(User.class));
        assertTrue(currentUser.getFollowing().contains(userToFollow));
        assertTrue(userToFollow.getFollowers().contains(currentUser));
    }

    @Test
    void unfollowUser_shouldSuccessfullyUnfollowUser() {
        // Arrange
        currentUser.getFollowing().add(userToFollow);
        userToFollow.getFollowers().add(currentUser);

        // --- FIX: The mock setup is MOVED here. ---
        when(mockUserDetails.getUsername()).thenReturn(currentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));

        // Act
        userService.unfollowUser("userToFollow", mockUserDetails);

        // Assert
        verify(userRepository, times(1)).save(currentUser);
        assertFalse(currentUser.getFollowing().contains(userToFollow));
        assertFalse(userToFollow.getFollowers().contains(currentUser));
    }

    @Test
    void followUser_shouldThrowException_whenFollowingSelf() {
        // Arrange
        // --- FIX: The mock setup is MOVED here. ---
        when(mockUserDetails.getUsername()).thenReturn(currentUser.getUsername());
        String username = "testUser";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.followUser(username, mockUserDetails);
        });

        assertEquals("You cannot follow yourself.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void followUser_shouldThrowException_whenUserToFollowNotFound() {
        // Arrange
        // --- FIX: The mock setup is MOVED here. ---
        when(mockUserDetails.getUsername()).thenReturn(currentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.followUser("nonExistentUser", mockUserDetails);
        });
    }

    @Test
    void getProfile_shouldReturnProfileDto_whenUserExists() {
        // Arrange
        // --- FIX: The mock setup is MOVED here. ---
        when(mockUserDetails.getUsername()).thenReturn(currentUser.getUsername());
        ProfileDto expectedProfile = new ProfileDto("userToFollow", 0, 0, false);
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(currentUser));
        when(userMapper.toProfileDto(userToFollow, currentUser)).thenReturn(expectedProfile);

        // Act
        ProfileDto actualProfile = userService.getProfile("userToFollow", mockUserDetails);

        // Assert
        assertNotNull(actualProfile);
        assertEquals("userToFollow", actualProfile.username());
        verify(userMapper, times(1)).toProfileDto(userToFollow, currentUser);
    }

    @Test
    void getProfile_withNullPrincipal_shouldReturnPublicProfile() {
        // This test verifies behavior for guest users (no logged-in principal)
        // Arrange
        ProfileDto expectedProfile = new ProfileDto("userToFollow", 0, 0, false);
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));
        when(userMapper.toProfileDto(userToFollow, null)).thenReturn(expectedProfile); // currentUser is null

        // Act
        ProfileDto actualProfile = userService.getProfile("userToFollow", null); // Pass null for the principal

        // Assert
        assertNotNull(actualProfile);
        verify(userRepository, never()).findByUsernameWithFollowing(any()); // Should not attempt to load a current user
    }

    @Test
    void getProfile_shouldThrowException_whenUserNotFound() {
        // Arrange
        // This test doesn't need the principal mock because it fails before using it.
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile("nonExistentUser", mockUserDetails);
        });
    }
}