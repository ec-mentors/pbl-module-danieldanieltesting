package com.promptdex.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user1 = new User();
        user1.setUsername("userOne");
        user1.setEmail("userone@test.com");
        user1.setPassword("password");
        user1.setProvider(AuthProvider.LOCAL);
        user2 = new User();
        user2.setUsername("userTwo");
        user2.setEmail("usertwo@test.com");
        user2.setPassword("password");
        user2.setProvider(AuthProvider.LOCAL);
        userRepository.saveAllAndFlush(List.of(user1, user2));
    }

    @Test
    void getProfile_asGuest_returnsPublicProfile() throws Exception {
        mockMvc.perform(get("/api/users/{username}/profile", user2.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("userTwo")))
                .andExpect(jsonPath("$.followerCount", is(0)))
                .andExpect(jsonPath("$.followingCount", is(0)))
                .andExpect(jsonPath("$.isFollowedByCurrentUser", is(false)));
    }

    @Test
    @WithMockUser(username = "userOne")
    void getProfile_asAuthenticatedUser_returnsCorrectFollowStatus() throws Exception {
        mockMvc.perform(get("/api/users/{username}/profile", user2.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowedByCurrentUser", is(false)));
    }

    @Test
    @WithMockUser(username = "userOne")
    void followUser_thenUnfollowUser_updatesProfileCorrectly() throws Exception {
        mockMvc.perform(post("/api/users/{username}/follow", user2.getUsername()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("userTwo")))
                .andExpect(jsonPath("$.followerCount", is(1)))
                .andExpect(jsonPath("$.isFollowedByCurrentUser", is(true)));
        mockMvc.perform(get("/api/users/{username}/profile", user2.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followerCount", is(1)))
                .andExpect(jsonPath("$.isFollowedByCurrentUser", is(true)));
        mockMvc.perform(post("/api/users/{username}/unfollow", user2.getUsername()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("userTwo")))
                .andExpect(jsonPath("$.followerCount", is(0)))
                .andExpect(jsonPath("$.isFollowedByCurrentUser", is(false)));
    }

    @Test
    @WithMockUser(username = "userOne")
    void followUser_whenAlreadyFollowing_doesNotChangeCount() throws Exception {
        user1.getFollowing().add(user2);
        userRepository.saveAndFlush(user1);
        mockMvc.perform(post("/api/users/{username}/follow", user2.getUsername()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followerCount", is(1)))
                .andExpect(jsonPath("$.isFollowedByCurrentUser", is(true)));
    }

    @Test
    @WithMockUser(username = "userOne")
    void followUser_selfFollow_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/{username}/follow", "userOne").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void followUser_asGuest_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users/{username}/follow", user1.getUsername()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}