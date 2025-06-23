package com.promptdex.api.dto;

/**
 * A DTO representing a user's public profile information.
 *
 * @param username              The user's username.
 * @param followerCount         The number of users following this user.
 * @param followingCount        The number of users this user is following.
 * @param isFollowedByCurrentUser True if the currently authenticated user is following this user, otherwise false.
 */
public record ProfileDto(
        String username,
        int followerCount,
        int followingCount,
        boolean isFollowedByCurrentUser
) {}