package com.promptdex.api.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptdex.api.dto.LoginRequest;
import com.promptdex.api.dto.RegisterRequest;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import com.promptdex.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("testuser@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setProvider(AuthProvider.LOCAL);
        userRepository.saveAndFlush(existingUser);
    }
    @Test
    void registerUser_withValidData_returnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"User registered successfully!\""));
        assertThat(userRepository.findByUsername("newuser")).isPresent();
    }
    @Test
    void registerUser_withDuplicateUsername_returnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser"); 
        request.setEmail("anotheremail@example.com");
        request.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Error: Username is already taken!")));
    }
    @Test
    void registerUser_withDuplicateEmail_returnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anotheruser");
        request.setEmail("testuser@example.com"); 
        request.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Error: Email is already in use!")));
    }
    @Test
    void loginUser_withValidCredentials_returnsOkWithToken() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseString).get("token").asText();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUsernameFromJWT(token)).isEqualTo("testuser");
    }
    @Test
    void loginUser_withInvalidPassword_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }
    @Test
    void loginUser_withNonExistentUser_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }
}