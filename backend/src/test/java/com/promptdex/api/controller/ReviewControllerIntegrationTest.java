package com.promptdex.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.ReviewRepository;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.JwtTokenProvider;
import com.promptdex.api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    private String userOneToken;
    private String userTwoToken;
    private User userOne;
    private User userTwo;
    private Prompt testPrompt;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        promptRepository.deleteAll();
        userRepository.deleteAll();

        userOne = new User();
        userOne.setUsername("userOne");
        userOne.setEmail("userone@test.com");
        userOne.setPassword(passwordEncoder.encode("password"));
        userOne.setProvider(AuthProvider.LOCAL);

        userTwo = new User();
        userTwo.setUsername("userTwo");
        userTwo.setEmail("usertwo@test.com");
        userTwo.setPassword(passwordEncoder.encode("password"));
        userTwo.setProvider(AuthProvider.LOCAL);
        userRepository.saveAllAndFlush(List.of(userOne, userTwo));

        testPrompt = new Prompt();
        testPrompt.setTitle("Test Prompt");
        testPrompt.setPromptText("A prompt for testing.");
        testPrompt.setCategory("Testing");
        testPrompt.setTargetAiModel("GPT-4");
        testPrompt.setAuthor(userTwo);
        promptRepository.saveAndFlush(testPrompt);

        userOneToken = generateTokenForUser(userOne);
        userTwoToken = generateTokenForUser(userTwo);
    }

    private String generateTokenForUser(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        return jwtTokenProvider.generateToken(authentication);
    }

    @Test
    void createReview_asDifferentUser_shouldSucceed() throws Exception {
        Map<String, Object> reviewRequest = new HashMap<>();
        reviewRequest.put("rating", 4);
        reviewRequest.put("comment", "This is a great prompt!");

        mockMvc.perform(post("/api/prompts/{promptId}/reviews", testPrompt.getId())
                        .with(csrf())
                        .header("Authorization", "Bearer " + userOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("This is a great prompt!"))
                .andExpect(jsonPath("$.authorUsername").value("userOne"));
    }

    @Test
    void createReview_asAuthor_shouldFailWith403() throws Exception {
        Map<String, Object> reviewRequest = new HashMap<>();
        reviewRequest.put("rating", 5);
        reviewRequest.put("comment", "I am reviewing my own prompt.");

        mockMvc.perform(post("/api/prompts/{promptId}/reviews", testPrompt.getId())
                        .with(csrf())
                        .header("Authorization", "Bearer " + userTwoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReview_whenAlreadyReviewed_shouldFailWith409() throws Exception { // <-- Renamed test for clarity
        // Arrange: Create one successful review from userOne
        Map<String, Object> firstReviewRequest = new HashMap<>();
        firstReviewRequest.put("rating", 5);
        firstReviewRequest.put("comment", "My first review.");

        mockMvc.perform(post("/api/prompts/{promptId}/reviews", testPrompt.getId())
                        .with(csrf())
                        .header("Authorization", "Bearer " + userOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstReviewRequest)))
                .andExpect(status().isCreated());

        // Prepare the second review attempt from the same user
        Map<String, Object> secondReviewRequest = new HashMap<>();
        secondReviewRequest.put("rating", 1);
        secondReviewRequest.put("comment", "Trying to review again.");

        // Act & Assert: userOne tries to review again
        mockMvc.perform(post("/api/prompts/{promptId}/reviews", testPrompt.getId())
                        .with(csrf())
                        .header("Authorization", "Bearer " + userOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondReviewRequest)))
                // --- THIS IS THE FIX ---
                // The API correctly returns a 409 Conflict in this scenario.
                .andExpect(status().isConflict());
    }
}