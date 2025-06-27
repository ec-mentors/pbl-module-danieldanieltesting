package com.promptdex.api.controller;

import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FeedControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PromptRepository promptRepository;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    private User userOne;
    private User userTwo;
    private User userThree;
    private Prompt promptFromFollowedUser;
    private Prompt promptFromUnfollowedUser;

    @BeforeEach
    void setUp() {
        promptRepository.deleteAll();
        userRepository.deleteAll();
        userOne = new User();
        userOne.setUsername("userOne");
        userOne.setEmail("userone@test.com");
        userOne.setPassword("password");
        userOne.setProvider(AuthProvider.LOCAL);
        userTwo = new User();
        userTwo.setUsername("userTwo");
        userTwo.setEmail("usertwo@test.com");
        userTwo.setPassword("password");
        userTwo.setProvider(AuthProvider.LOCAL);
        userThree = new User();
        userThree.setUsername("userThree");
        userThree.setEmail("userthree@test.com");
        userThree.setPassword("password");
        userThree.setProvider(AuthProvider.LOCAL);
        userRepository.saveAllAndFlush(List.of(userOne, userTwo, userThree));
        promptFromFollowedUser = createPrompt("Prompt from Followed User", userTwo, Instant.now().minus(1, ChronoUnit.DAYS));
        promptFromUnfollowedUser = createPrompt("Prompt from Unfollowed User", userThree, Instant.now());
        promptRepository.saveAllAndFlush(List.of(promptFromFollowedUser, promptFromUnfollowedUser));
    }

    private Prompt createPrompt(String title, User author, Instant createdAt) {
        Prompt p = new Prompt();
        p.setTitle(title);
        p.setPromptText("Test text");
        p.setDescription("Test desc");
        p.setTargetAiModel("GPT-4");
        p.setCategory("Testing");
        p.setAuthor(author);
        p.setCreatedAt(createdAt);
        p.setUpdatedAt(createdAt);
        return p;
    }

    @Test
    void getUserFeed_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "userOne")
    void getUserFeed_whenFollowingUsers_returnsOnlyFollowedUsersPrompts() throws Exception {
        userOne.follow(userTwo);
        userRepository.saveAndFlush(userOne);
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].prompt.id", is(promptFromFollowedUser.getId().toString())))
                .andExpect(jsonPath("$.content[0].prompt.title", is("Prompt from Followed User")))
                .andExpect(jsonPath("$.content[0].eventType", is("NEW_PROMPT_FROM_FOLLOWING")));
    }

    @Test
    @WithMockUser(username = "userOne")
    void getUserFeed_whenFollowingNoOne_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @WithMockUser(username = "userOne")
    void getUserFeed_withPagination_returnsCorrectPage() throws Exception {
        Prompt olderPrompt = createPrompt("Older Prompt", userTwo, Instant.now().minus(2, ChronoUnit.HOURS));
        Prompt newerPrompt = createPrompt("Newer Prompt", userTwo, Instant.now().minus(1, ChronoUnit.HOURS));
        promptRepository.saveAllAndFlush(List.of(olderPrompt, newerPrompt));
        userOne.follow(userTwo);
        userRepository.saveAndFlush(userOne);
        mockMvc.perform(get("/api/feed?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.content[0].prompt.title", is("Newer Prompt")));
        mockMvc.perform(get("/api/feed?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].prompt.title", is("Older Prompt")));
    }
}