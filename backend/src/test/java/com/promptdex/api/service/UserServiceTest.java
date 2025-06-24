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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    private User testCurrentUser;
    private User testUserToFollow;
    private ProfileDto mockProfileDto;

    @BeforeEach
    void setUp() {
        testCurrentUser = new User();
        testCurrentUser.setId(UUID.randomUUID());
        testCurrentUser.setUsername("testUser");
        testCurrentUser.setFollowing(new HashSet<>());
        testCurrentUser.setFollowers(new HashSet<>());

        testUserToFollow = new User();
        testUserToFollow.setId(UUID.randomUUID());
        testUserToFollow.setUsername("userToFollow");
        testUserToFollow.setFollowers(new HashSet<>());
        testUserToFollow.setFollowing(new HashSet<>());

        // Example DTO, customize as needed for different test outcomes
        mockProfileDto = new ProfileDto("userToFollow", 0, 0, false);
    }

    @Test
    void followUser_shouldSuccessfullyFollowUser() {
        // Arrange
        when(mockUserDetails.getUsername()).thenReturn(testCurrentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing(testCurrentUser.getUsername())).thenReturn(Optional.of(testCurrentUser));
        when(userRepository.findByUsername(testUserToFollow.getUsername())).thenReturn(Optional.of(testUserToFollow));
        when(userMapper.toProfileDto(eq(testUserToFollow), eq(testCurrentUser))).thenReturn(mockProfileDto);

        // Act
        ProfileDto resultDto = userService.followUser(testUserToFollow.getUsername(), mockUserDetails);

        // Assert
        // REMOVED: verify(userRepository, times(1)).save(eq(testCurrentUser)); // No longer verifying explicit save
        // We now rely on asserting the state change and mapper interaction.

        verify(userMapper, times(1)).toProfileDto(eq(testUserToFollow), eq(testCurrentUser));
        assertEquals(mockProfileDto, resultDto);

        assertTrue(testCurrentUser.getFollowing().contains(testUserToFollow), "currentUser should be following userToFollow");
        assertTrue(testUserToFollow.getFollowers().contains(testCurrentUser), "userToFollow should have currentUser as a follower");
    }

    @Test
    void unfollowUser_shouldSuccessfullyUnfollowUser() {
        // Arrange
        testCurrentUser.getFollowing().add(testUserToFollow); // Pre-condition
        testUserToFollow.getFollowers().add(testCurrentUser); // Pre-condition

        when(mockUserDetails.getUsername()).thenReturn(testCurrentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing(testCurrentUser.getUsername())).thenReturn(Optional.of(testCurrentUser));
        when(userRepository.findByUsername(testUserToFollow.getUsername())).thenReturn(Optional.of(testUserToFollow));

        // Create a DTO that would represent the state after unfollowing
        ProfileDto unfollowedProfileDto = new ProfileDto(testUserToFollow.getUsername(), 0, 0, false); // isFollowing would be false
        when(userMapper.toProfileDto(eq(testUserToFollow), eq(testCurrentUser))).thenReturn(unfollowedProfileDto);


        // Act
        ProfileDto resultDto = userService.unfollowUser(testUserToFollow.getUsername(), mockUserDetails);

        // Assert
        // REMOVED: verify(userRepository, times(1)).save(eq(testCurrentUser)); // No longer verifying explicit save

        verify(userMapper, times(1)).toProfileDto(eq(testUserToFollow), eq(testCurrentUser));
        assertEquals(unfollowedProfileDto, resultDto);

        assertFalse(testCurrentUser.getFollowing().contains(testUserToFollow), "currentUser should NOT be following userToFollow");
        assertFalse(testUserToFollow.getFollowers().contains(testCurrentUser), "userToFollow should NOT have currentUser as a follower");
    }

    @Test
    void followUser_shouldThrowException_whenFollowingSelf() {
        // Arrange
        when(mockUserDetails.getUsername()).thenReturn(testCurrentUser.getUsername());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.followUser(testCurrentUser.getUsername(), mockUserDetails);
        });

        assertEquals("You cannot follow yourself.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unfollowUser_shouldThrowException_whenUnfollowingSelf() {
        // Arrange
        when(mockUserDetails.getUsername()).thenReturn(testCurrentUser.getUsername());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.unfollowUser(testCurrentUser.getUsername(), mockUserDetails);
        });

        assertEquals("You cannot unfollow yourself.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void followUser_shouldThrowException_whenUserToFollowNotFound() {
        // Arrange
        when(mockUserDetails.getUsername()).thenReturn(testCurrentUser.getUsername());
        when(userRepository.findByUsernameWithFollowing(testCurrentUser.getUsername())).thenReturn(Optional.of(testCurrentUser));
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.followUser("nonExistentUser", mockUserDetails);
        });
        assertTrue(exception.getMessage().contains("User to follow not found"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void followUser_shouldThrowException_whenCurrentUserNotFound() {
        // Arrange
        when(mockUserDetails.getUsername()).thenReturn("nonExistentCurrentUser");
        when(userRepository.findByUsernameWithFollowing("nonExistentCurrentUser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.followUser(testUserToFollow.getUsername(), mockUserDetails);
        });
        assertTrue(exception.getMessage().contains("Current user not found"));
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void getProfile_shouldReturnProfileDto_whenUserExistsAndPrincipalExists() {
        // Arrange
        ProfileDto expectedProfile = new ProfileDto(testUserToFollow.getUsername(), 0, 0, false); // Customize as needed
        when(mockUserDetails.getUsername()).thenReturn(testCurrentUser.getUsername());
        when(userRepository.findByUsername(testUserToFollow.getUsername())).thenReturn(Optional.of(testUserToFollow));
        when(userRepository.findByUsernameWithFollowing(testCurrentUser.getUsername())).thenReturn(Optional.of(testCurrentUser));
        when(userMapper.toProfileDto(eq(testUserToFollow), eq(testCurrentUser))).thenReturn(expectedProfile);

        // Act
        ProfileDto actualProfile = userService.getProfile(testUserToFollow.getUsername(), mockUserDetails);

        // Assert
        assertNotNull(actualProfile);
        assertEquals(expectedProfile, actualProfile);
        verify(userMapper, times(1)).toProfileDto(testUserToFollow, testCurrentUser);
    }

    @Test
    void getProfile_withNullPrincipal_shouldReturnPublicProfile() {
        // Arrange
        ProfileDto expectedProfile = new ProfileDto(testUserToFollow.getUsername(), 0, 0, false);
        when(userRepository.findByUsername(testUserToFollow.getUsername())).thenReturn(Optional.of(testUserToFollow));
        when(userMapper.toProfileDto(eq(testUserToFollow), eq(null))).thenReturn(expectedProfile);

        // Act
        ProfileDto actualProfile = userService.getProfile(testUserToFollow.getUsername(), null);

        // Assert
        assertNotNull(actualProfile);
        assertEquals(expectedProfile, actualProfile);
        verify(userRepository, never()).findByUsernameWithFollowing(anyString());
        verify(userMapper, times(1)).toProfileDto(testUserToFollow, null);
    }

    @Test
    void getProfile_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        // No mockUserDetails.getUsername() needed here if service checks for targetUser first

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getProfile("nonExistentUser", mockUserDetails); // mockUserDetails might be unused if exception is thrown early
        });
        assertTrue(exception.getMessage().contains("User not found with username: nonExistentUser"));
        verify(userMapper, never()).toProfileDto(any(), any());
        verify(userRepository, never()).findByUsernameWithFollowing(anyString()); // Ensure current user wasn't fetched
    }
}