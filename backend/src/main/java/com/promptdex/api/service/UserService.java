package com.promptdex.api.service;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.UserMapper;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(String username, UserPrincipal principal) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                    .orElse(null);
        }

        return userMapper.toProfileDto(targetUser, currentUser);
    }

    public ProfileDto followUser(String usernameToFollow, UserPrincipal principal) {
        if (principal.getUsername().equals(usernameToFollow)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));

        User userToFollow = userRepository.findByUsername(usernameToFollow)
                .orElseThrow(() -> new ResourceNotFoundException("User to follow not found with username: " + usernameToFollow));

        currentUser.getFollowing().add(userToFollow);
        // Use saveAndFlush to ensure the change is written to the database immediately within this transaction
        userRepository.saveAndFlush(currentUser);

        // --- THE FIX ---
        // Re-fetch the user whose profile is being returned to get the updated follower count.
        User updatedUserToFollow = userRepository.findById(userToFollow.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User to follow not found after update: " + usernameToFollow));

        return userMapper.toProfileDto(updatedUserToFollow, currentUser);
    }

    public ProfileDto unfollowUser(String usernameToUnfollow, UserPrincipal principal) {
        if (principal.getUsername().equals(usernameToUnfollow)) {
            throw new IllegalArgumentException("You cannot unfollow yourself.");
        }

        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));

        User userToUnfollow = userRepository.findByUsername(usernameToUnfollow)
                .orElseThrow(() -> new ResourceNotFoundException("User to unfollow not found with username: " + usernameToUnfollow));

        currentUser.getFollowing().remove(userToUnfollow);
        userRepository.saveAndFlush(currentUser);

        // --- THE FIX ---
        // Re-fetch the user whose profile is being returned to get the updated follower count.
        User updatedUserToUnfollow = userRepository.findById(userToUnfollow.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User to unfollow not found after update: " + usernameToUnfollow));

        return userMapper.toProfileDto(updatedUserToUnfollow, currentUser);
    }
}