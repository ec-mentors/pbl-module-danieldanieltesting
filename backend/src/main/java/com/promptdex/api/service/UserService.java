package com.promptdex.api.service;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.dto.UserAdminViewDto;
import com.promptdex.api.exception.ResourceNotFoundException;
import com.promptdex.api.mapper.UserMapper;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils

import java.util.Set;
import java.util.UUID;

@Service
@Transactional // Applies to all public methods by default
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
            currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                    .orElseGet(() -> userRepository.findByUsername(principal.getUsername()).orElse(null));
        }

        return userMapper.toProfileDto(targetUser, currentUser);
    }

    public ProfileDto followUser(String usernameToFollow, UserDetails principal) {
        if (principal.getUsername().equals(usernameToFollow)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found: " + principal.getUsername()));
        User userToFollow = userRepository.findByUsername(usernameToFollow)
                .orElseThrow(() -> new ResourceNotFoundException("User to follow not found: " + usernameToFollow));

        currentUser.follow(userToFollow);
        return userMapper.toProfileDto(userToFollow, currentUser);
    }

    public ProfileDto unfollowUser(String usernameToUnfollow, UserDetails principal) {
        if (principal.getUsername().equals(usernameToUnfollow)) {
            throw new IllegalArgumentException("You cannot unfollow yourself.");
        }

        User currentUser = userRepository.findByUsernameWithFollowing(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found: " + principal.getUsername()));
        User userToUnfollow = userRepository.findByUsername(usernameToUnfollow)
                .orElseThrow(() -> new ResourceNotFoundException("User to unfollow not found: " + usernameToUnfollow));

        currentUser.unfollow(userToUnfollow);
        return userMapper.toProfileDto(userToUnfollow, currentUser);
    }

    // --- UPDATED ADMIN METHOD ---
    /**
     * Retrieves a paginated list of all users for administrative purposes,
     * optionally filtered by a search term.
     *
     * @param searchTerm Optional term to search by username or email.
     * @param pageable   Pagination information.
     * @return A page of UserAdminViewDto.
     */
    @Transactional(readOnly = true)
    public Page<UserAdminViewDto> getAllUsersAsAdmin(String searchTerm, Pageable pageable) {
        Page<User> usersPage;
        if (StringUtils.hasText(searchTerm)) { // Use StringUtils for robust check
            usersPage = userRepository.findAllAdminSearch(searchTerm.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }
        // Manual mapping for now, or enhance UserMapper
        return usersPage.map(user -> new UserAdminViewDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                user.getProvider()
        ));
    }

    /**
     * Updates the roles for a given user.
     * Restricted to users with ROLE_ADMIN.
     *
     * @param userId   The ID of the user whose roles are to be updated.
     * @param newRoles The new set of roles for the user.
     * @return The updated User entity, mapped to UserAdminViewDto.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminViewDto updateUserRoles(UUID userId, Set<String> newRoles) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userToUpdate.setRoles(newRoles);
        User updatedUser = userRepository.save(userToUpdate);

        return new UserAdminViewDto(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRoles(),
                updatedUser.getProvider()
        );
    }
    // --- END ADMIN METHODS ---
}