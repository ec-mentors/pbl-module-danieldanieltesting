package com.promptdex.api.mapper;

import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Maps a target User entity to a ProfileDto.
     *
     * @param targetUser The user whose profile is being viewed.
     * @param currentUser The currently authenticated user (can be null for guests).
     * @return A ProfileDto containing the target user's profile information.
     */
    public ProfileDto toProfileDto(User targetUser, User currentUser) {
        if (targetUser == null) {
            return null;
        }

        boolean isFollowed = false;
        if (currentUser != null && currentUser.getFollowing() != null) {
            // Check if the current user's 'following' set contains the target user.
            isFollowed = currentUser.getFollowing().stream()
                    .anyMatch(followedUser -> followedUser.getId().equals(targetUser.getId()));
        }

        return new ProfileDto(
                targetUser.getUsername(),
                targetUser.getFollowers() != null ? targetUser.getFollowers().size() : 0,
                targetUser.getFollowing() != null ? targetUser.getFollowing().size() : 0,
                isFollowed
        );
    }
}