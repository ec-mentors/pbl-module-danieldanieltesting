// COMPLETE FILE: src/main/java/com/promptdex/api/service/UserService.java

package com.promptdex.api.service;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.UserMapper;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ProfileDto getProfile(String username, UserDetails principal) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername()).orElse(null);
        }

        return userMapper.toProfileDto(targetUser, currentUser);
    }

    public ProfileDto followUser(String usernameToFollow, UserDetails principal) {
        if (principal.getUsername().equals(usernameToFollow)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
        User userToFollow = userRepository.findByUsername(usernameToFollow)
                .orElseThrow(() -> new ResourceNotFoundException("User to follow not found."));

        // --- THE FIX ---
        // Use the new helper method to synchronize both sides of the relationship
        currentUser.follow(userToFollow);
        userRepository.save(currentUser);

        // No need to re-fetch. The userToFollow object in memory is now correct.
        return userMapper.toProfileDto(userToFollow, currentUser);
    }

    public ProfileDto unfollowUser(String usernameToUnfollow, UserDetails principal) {
        if (principal.getUsername().equals(usernameToUnfollow)) {
            throw new IllegalArgumentException("You cannot unfollow yourself.");
        }

        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
        User userToUnfollow = userRepository.findByUsername(usernameToUnfollow)
                .orElseThrow(() -> new ResourceNotFoundException("User to unfollow not found."));

        // --- THE FIX ---
        // Use the new helper method here as well
        currentUser.unfollow(userToUnfollow);
        userRepository.save(currentUser);

        return userMapper.toProfileDto(userToUnfollow, currentUser);
    }
}