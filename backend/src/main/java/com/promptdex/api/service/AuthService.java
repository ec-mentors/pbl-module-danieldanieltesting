package com.promptdex.api.service;

import com.promptdex.api.dto.LoginRequest;
import com.promptdex.api.dto.RegisterRequest;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // Inject dependencies via constructor
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user in the system.
     * @param registerRequest DTO containing registration details.
     * @throws IllegalStateException if username or email is already taken.
     */

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        // This validation logic is likely already here, just ensure it's present
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User newUser = new User();

        // <-- CHANGE HERE: Use standard JavaBean getters
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user's credentials.
     * @param loginRequest DTO containing login credentials.
     * @return A Spring Security Authentication object if successful.
     */
    public Authentication loginUser(LoginRequest loginRequest) {
        // 1. Use AuthenticationManager to authenticate the user's credentials.
        // This process leverages the CustomUserDetailsService and PasswordEncoder beans you've already configured.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        // 2. If authentication is successful, the manager returns a fully populated Authentication object.
        return authentication;
    }
}