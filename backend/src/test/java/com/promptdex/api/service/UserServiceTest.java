package com.promptdex.api.service;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.UserMapper;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserService.
 * We use Mockito to simulate the behavior of the repository and mapper,
 * allowing us to test the service's logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @Mock creates a mock implementation for the class it's annotating.
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    // @InjectMocks creates an instance of the class and injects the mocks that are created with @Mock into it.
    @InjectMocks
    private UserService userService;

    private User testUser;
    private User userToFollow;
    private UserPrincipal testUserPrincipal;

    // This method runs before each test, setting up common objects.
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testUser");
        testUser.setFollowing(new HashSet<>()); // Initialize the set

        userToFollow = new User();
        userToFollow.setId(UUID.randomUUID());
        userToFollow.setUsername("userToFollow");
        userToFollow.setFollowers(new HashSet<>());

        // UserPrincipal is what Spring Security provides to our controllers.
        testUserPrincipal = new UserPrincipal(testUser);
    }

    @Test
    void followUser_shouldSuccessfullyFollowUser() {
        // Arrange (Given): Define the behavior of our mocks.
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));
        when(userRepository.findById(userToFollow.getId())).thenReturn(Optional.of(userToFollow)); // For the re-fetch

        // Act (When): Call the method we are testing.
        userService.followUser("userToFollow", testUserPrincipal);

        // Assert (Then): Verify the results.
        // Verify that saveAndFlush was called exactly once on the repository.
        verify(userRepository, times(1)).saveAndFlush(testUser);
        // Verify that the mapper was called to create the response DTO.
        verify(userMapper, times(1)).toProfileDto(any(User.class), any(User.class));
        // Assert that the user to follow is now in the test user's following list.
        assertTrue(testUser.getFollowing().contains(userToFollow));
    }

    @Test
    void followUser_shouldThrowException_whenFollowingSelf() {
        // Arrange
        String username = "testUser";

        // Act & Assert
        // We assert that calling the method with the same username throws an IllegalArgumentException.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.followUser(username, testUserPrincipal);
        });

        assertEquals("You cannot follow yourself.", exception.getMessage());
        // Verify that the save method was never called.
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void followUser_shouldThrowException_whenUserToFollowNotFound() {
        // Arrange
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.followUser("nonExistentUser", testUserPrincipal);
        });
    }

    @Test
    void unfollowUser_shouldSuccessfullyUnfollowUser() {
        // Arrange
        // Start with the testUser already following the userToFollow.
        testUser.getFollowing().add(userToFollow);

        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));
        when(userRepository.findById(userToFollow.getId())).thenReturn(Optional.of(userToFollow)); // For the re-fetch

        // Act
        userService.unfollowUser("userToFollow", testUserPrincipal);

        // Assert
        verify(userRepository, times(1)).saveAndFlush(testUser);
        assertFalse(testUser.getFollowing().contains(userToFollow));
    }

    @Test
    void getProfile_shouldReturnProfileDto_whenUserExists() {
        // Arrange
        ProfileDto expectedProfile = new ProfileDto("userToFollow", 0, 0, false);
        when(userRepository.findByUsername("userToFollow")).thenReturn(Optional.of(userToFollow));
        when(userRepository.findByUsernameWithFollowing("testUser")).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileDto(userToFollow, testUser)).thenReturn(expectedProfile);

        // Act
        ProfileDto actualProfile = userService.getProfile("userToFollow", testUserPrincipal);

        // Assert
        assertNotNull(actualProfile);
        assertEquals("userToFollow", actualProfile.username());
        verify(userMapper, times(1)).toProfileDto(userToFollow, testUser);
    }

    @Test
    void getProfile_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile("nonExistentUser", testUserPrincipal);
        });
    }
}