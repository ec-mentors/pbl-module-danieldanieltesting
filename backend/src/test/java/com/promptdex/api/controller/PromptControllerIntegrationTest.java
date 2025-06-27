package com.promptdex.api.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptdex.api.dto.CreatePromptRequest;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    private User testAuthor;
    private User otherUser;
    private Prompt prompt1;
    private Prompt prompt2;
    @BeforeEach
    void setUp() {
        testAuthor = new User();
        testAuthor.setUsername("testAuthor");
        testAuthor.setEmail("author@test.com");
        testAuthor.setPassword("password");
        testAuthor.setProvider(AuthProvider.LOCAL);
        otherUser = new User();
        otherUser.setUsername("otherUser");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        otherUser.setProvider(AuthProvider.LOCAL);
        userRepository.saveAllAndFlush(List.of(testAuthor, otherUser));
        Tag tag1 = new Tag(null, "testing", null);
        Tag tag2 = new Tag(null, "java", null);
        tagRepository.saveAllAndFlush(List.of(tag1, tag2));
        prompt1 = new Prompt();
        prompt1.setTitle("Test Prompt Title 1");
        prompt1.setPromptText("Generate a simple Spring Boot app.");
        prompt1.setDescription("A description for testing.");
        prompt1.setTargetAiModel("GPT-4");
        prompt1.setCategory("Development");
        prompt1.setAuthor(testAuthor);
        prompt1.getTags().add(tag1); 
        prompt2 = new Prompt();
        prompt2.setTitle("Java Prompt Title 2");
        prompt2.setPromptText("Explain dependency injection in Java.");
        prompt2.setDescription("Another description.");
        prompt2.setTargetAiModel("Claude 3");
        prompt2.setCategory("Education");
        prompt2.setAuthor(testAuthor);
        prompt2.getTags().add(tag2);
        promptRepository.saveAllAndFlush(List.of(prompt1, prompt2));
    }
    @Test
    void getPromptById_whenPromptExists_returnsPrompt() throws Exception {
        mockMvc.perform(get("/api/prompts/{id}", prompt1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(prompt1.getId().toString())))
                .andExpect(jsonPath("$.title", is("Test Prompt Title 1")))
                .andExpect(jsonPath("$.authorUsername", is("testAuthor")));
    }
    @Test
    void createPrompt_withoutAuth_returnsUnauthorized() throws Exception {
        CreatePromptRequest request = new CreatePromptRequest("New API Prompt", "Text...", "Description...", "GPT-4", "Testing");
        mockMvc.perform(post("/api/prompts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(username = "testAuthor")
    void updatePrompt_asAuthor_returnsOk() throws Exception {
        CreatePromptRequest request = new CreatePromptRequest("Updated Title", "Updated Text", "Updated Desc", "GPT-4", "Updated Category");
        mockMvc.perform(put("/api/prompts/{id}", prompt1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }
    @Test
    @WithMockUser(username = "testAuthor")
    void deletePrompt_asAuthor_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/prompts/{id}", prompt1.getId()).with(csrf()))
                .andExpect(status().isNoContent());
    }
    @Test
    @WithMockUser(username = "testAuthor")
    void createPrompt_withValidData_returnsCreated() throws Exception {
        CreatePromptRequest request = new CreatePromptRequest("New API Prompt", "Text...", "Description...", "GPT-4", "Testing");
        mockMvc.perform(post("/api/prompts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
    @Test
    @WithMockUser(username = "otherUser")
    void updatePrompt_asDifferentUser_returnsForbidden() throws Exception {
        CreatePromptRequest request = new CreatePromptRequest("Attempted Update", "...", "...", "model", "category");
        mockMvc.perform(put("/api/prompts/{id}", prompt1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(username = "otherUser")
    void deletePrompt_asDifferentUser_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/prompts/{id}", prompt1.getId()).with(csrf()))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(username = "otherUser")
    void bookmarkPrompt_whenNotBookmarked_returnsOk() throws Exception {
        mockMvc.perform(post("/api/prompts/{id}/bookmark", prompt1.getId()).with(csrf()))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(username = "otherUser")
    void unbookmarkPrompt_whenBookmarked_returnsNoContent() throws Exception {
        otherUser.getBookmarkedPrompts().add(prompt1);
        userRepository.save(otherUser);
        mockMvc.perform(delete("/api/prompts/{id}/bookmark", prompt1.getId()).with(csrf()))
                .andExpect(status().isNoContent());
    }
}