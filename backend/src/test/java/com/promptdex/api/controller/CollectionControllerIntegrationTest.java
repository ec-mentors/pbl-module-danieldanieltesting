package com.promptdex.api.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptdex.api.dto.CreateCollectionRequest;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.Collection;
import com.promptdex.api.model.Prompt;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.CollectionRepository;
import com.promptdex.api.repository.PromptRepository;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CollectionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PromptRepository promptRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    private User userOne;
    private User userTwo;
    private Prompt promptOne;
    private Collection collectionOne;
    @BeforeEach
    void setUp() {
        collectionRepository.deleteAll();
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
        userRepository.saveAllAndFlush(List.of(userOne, userTwo));
        promptOne = new Prompt();
        promptOne.setTitle("Prompt for Collections");
        promptOne.setPromptText("Text here.");
        promptOne.setDescription("Desc here.");
        promptOne.setTargetAiModel("GPT-4");
        promptOne.setCategory("Testing");
        promptOne.setAuthor(userOne);
        promptRepository.saveAndFlush(promptOne);
        collectionOne = new Collection("My Favorite Prompts", "A collection of great prompts.", userOne);
        collectionRepository.saveAndFlush(collectionOne);
    }
    @Test
    @WithMockUser(username = "userOne")
    void createCollection_withValidData_returnsCreated() throws Exception {
        CreateCollectionRequest request = new CreateCollectionRequest("New Test Collection", "A description.");
        mockMvc.perform(post("/api/collections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Test Collection")))
                .andExpect(jsonPath("$.promptCount", is(0)));
    }
    @Test
    @WithMockUser(username = "userOne")
    void createCollection_whenNameAlreadyExistsForUser_returnsConflict() throws Exception {
        CreateCollectionRequest request = new CreateCollectionRequest("My Favorite Prompts", "Another description.");
        mockMvc.perform(post("/api/collections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    @Test
    void createCollection_withoutAuth_returnsUnauthorized() throws Exception {
        CreateCollectionRequest request = new CreateCollectionRequest("Unauthorized Collection", "A description.");
        mockMvc.perform(post("/api/collections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(username = "userOne")
    void getMyCollections_returnsUserCollections() throws Exception {
        mockMvc.perform(get("/api/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("My Favorite Prompts")));
    }
    @Test
    @WithMockUser(username = "userOne")
    void getCollectionById_asOwner_returnsCollectionDetail() throws Exception {
        mockMvc.perform(get("/api/collections/{collectionId}", collectionOne.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(collectionOne.getId().toString())))
                .andExpect(jsonPath("$.name", is("My Favorite Prompts")));
    }
    @Test
    @WithMockUser(username = "userTwo")
    void getCollectionById_asDifferentUser_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/collections/{collectionId}", collectionOne.getId()))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "userOne")
    void updateCollection_asOwner_returnsOk() throws Exception {
        CreateCollectionRequest request = new CreateCollectionRequest("Updated Name", "Updated Description");
        mockMvc.perform(put("/api/collections/{collectionId}", collectionOne.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.description", is("Updated Description")));
    }
    @Test
    @WithMockUser(username = "userTwo")
    void updateCollection_asDifferentUser_returnsNotFound() throws Exception {
        CreateCollectionRequest request = new CreateCollectionRequest("Attempted Update", "");
        mockMvc.perform(put("/api/collections/{collectionId}", collectionOne.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "userOne")
    void deleteCollection_asOwner_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/collections/{collectionId}", collectionOne.getId()).with(csrf()))
                .andExpect(status().isNoContent());
    }
    @Test
    @WithMockUser(username = "userTwo")
    void deleteCollection_asDifferentUser_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/collections/{collectionId}", collectionOne.getId()).with(csrf()))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "userOne")
    void addPrompt_thenRemovePrompt_modifiesCollectionCorrectly() throws Exception {
        mockMvc.perform(put("/api/collections/{collectionId}/prompts/{promptId}", collectionOne.getId(), promptOne.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompts", hasSize(1)))
                .andExpect(jsonPath("$.prompts[0].id", is(promptOne.getId().toString())));
        mockMvc.perform(get("/api/collections/{collectionId}", collectionOne.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompts", hasSize(1)));
        mockMvc.perform(delete("/api/collections/{collectionId}/prompts/{promptId}", collectionOne.getId(), promptOne.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/collections/{collectionId}", collectionOne.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompts", hasSize(0)));
    }
    @Test
    @WithMockUser(username = "userTwo")
    void addPromptToCollection_asDifferentUser_returnsNotFound() throws Exception {
        mockMvc.perform(put("/api/collections/{collectionId}/prompts/{promptId}", collectionOne.getId(), promptOne.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}