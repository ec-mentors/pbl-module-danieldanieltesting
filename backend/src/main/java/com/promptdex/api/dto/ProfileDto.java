package com.promptdex.api.dto;
public record ProfileDto(
        String username,
        int followerCount,
        int followingCount,
        boolean isFollowedByCurrentUser
) {}