package com.promptdex.api.service;

import com.promptdex.api.dto.LoginRequest;
import com.promptdex.api.dto.RegisterRequest;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_whenUsernameAndEmailAreNew_shouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@email.com");
        request.setPassword("password");
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        authService.registerUser(request);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_whenUsernameExists_shouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setEmail("new@email.com");
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.registerUser(request);
        });
        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_whenEmailExists_shouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@email.com");
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            authService.registerUser(request);
        });
        assertEquals("Error: Email is already in use!", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginUser_shouldCallAuthenticationManager() {
        LoginRequest loginRequest = new LoginRequest("user", "password");
        authService.loginUser(loginRequest);
        verify(authenticationManager, times(1)).authenticate(any());
    }
}