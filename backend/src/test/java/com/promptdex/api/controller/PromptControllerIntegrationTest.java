// COMPLETED WORK: PromptControllerIntegrationTest.java

package com.promptdex.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptdex.api.dto.PromptRequest;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.Tag;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.PromptRepository;
import com.promptdex.api.repository.TagRepository;
import com.promptdex.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PromptControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testAuthor;
    private User otherUser;
    private Prompt prompt1;
    private Prompt prompt2;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        // Clear previous test data
        promptRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();

        // Create Users
        testAuthor = new User("testAuthor", "author@test.com", "password", AuthProvider.LOCAL);
        otherUser = new User("otherUser", "other@test.com", "password", AuthProvider.LOCAL);
        userRepository.saveAll(List.of(testAuthor, otherUser));

        // Create Tags
        tag1 = new Tag("testing");
        tag2 = new Tag("java");
        tagRepository.saveAll(List.of(tag1, tag2));

        // Create Prompts
        prompt1 = new Prompt("Test Prompt Title 1", "A description for testing.", "Generate a simple Spring Boot app.", testAuthor, Set.of(tag1));
        prompt2 = new Prompt("Java Prompt Title 2", "Another description.", "Explain dependency injection in Java.", testAuthor, Set.of(tag2));
        promptRepository.saveAll(List.of(prompt1, prompt2));
    }

    @Test
    void getPromptById_whenPromptExists_returnsPrompt() throws Exception {
        mockMvc.perform(get("/api/prompts/{id}", prompt1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(prompt1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Prompt Title 1")))
                .andExpect(jsonPath("$.author.username", is("testAuthor")));
    }

    @Test
    void getPromptById_whenPromptDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/prompts/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchPrompts_withoutParams_returnsAllPrompts() throws Exception {
        mockMvc.perform(get("/api/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }



    @Test
    void searchPrompts_withKeyword_returnsMatchingPrompts() throws Exception {
        mockMvc.perform(get("/api/prompts").param("q", "spring boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Test Prompt Title 1")));
    }

    @Test
    void searchPrompts_withTag_returnsMatchingPrompts() throws Exception {
        mockMvc.perform(get("/api/prompts").param("tags", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Java Prompt Title 2")));
    }

    @Test
    @WithMockUser(username = "testAuthor")
    void createPrompt_withValidData_returnsCreated() throws Exception {
        PromptRequest request = new PromptRequest("New API Prompt", "Description...", "Text...", Set.of("new", "api"));

        mockMvc.perform(post("/api/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New API Prompt")))
                .andExpect(jsonPath("$.author.username", is("testAuthor")))
                .andExpect(jsonPath("$.tags", hasItem("new")));
    }

    @Test
    void createPrompt_withoutAuth_returnsUnauthorized() throws Exception {
        PromptRequest request = new PromptRequest("New API Prompt", "Description...", "Text...", Set.of("new", "api"));
        mockMvc.perform(post("/api/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testAuthor")
    void updatePrompt_asAuthor_returnsOk() throws Exception {
        PromptRequest request = new PromptRequest("Updated Title", "Updated Desc", "Updated Text", Set.of("updated"));

        mockMvc.perform(put("/api/prompts/{id}", prompt1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.tags", hasItem("updated")));
    }

    @Test
    @WithMockUser(username = "otherUser")
    void updatePrompt_asDifferentUser_returnsForbidden() throws Exception {
        PromptRequest request = new PromptRequest("Attempted Update", "...", "...", Set.of());

        mockMvc.perform(put("/api/prompts/{id}", prompt1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testAuthor")
    void deletePrompt_asAuthor_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/prompts/{id}", prompt1.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "otherUser")
    void deletePrompt_asDifferentUser_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/prompts/{id}", prompt1.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otherUser")
    void bookmarkPrompt_whenNotBookmarked_returnsOk() throws Exception {
        mockMvc.perform(post("/api/prompts/{id}/bookmark", prompt1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(otherUser.getId().intValue())))
                .andExpect(jsonPath("$.bookmarkedPrompts", hasSize(1)))
                .andExpect(jsonPath("$.bookmarkedPrompts[0].id", is(prompt1.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "otherUser")
    void unbookmarkPrompt_whenBookmarked_returnsNoContent() throws Exception {
        // First, bookmark it
        otherUser.getBookmarkedPrompts().add(prompt1);
        userRepository.save(otherUser);

        mockMvc.perform(delete("/api/prompts/{id}/bookmark", prompt1.getId()))
                .andExpect(status().isNoContent());
    }
}