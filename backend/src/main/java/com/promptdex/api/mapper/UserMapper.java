package com.promptdex.api.mapper;
import com.promptdex.api.dto.ProfileDto;
import com.promptdex.api.model.User;
import org.springframework.stereotype.Component;
@Component
public class UserMapper {
    public ProfileDto toProfileDto(User targetUser, User currentUser) {
        if (targetUser == null) {
            return null;
        }
        boolean isFollowed = false;
        if (currentUser != null && currentUser.getFollowing() != null) {
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