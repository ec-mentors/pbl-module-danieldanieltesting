package com.promptdex.api.service;

import com.promptdex.api.dto.LoginRequest;
import com.promptdex.api.dto.RegisterRequest;
import com.promptdex.api.model.AuthProvider;
import com.promptdex.api.model.User;
import com.promptdex.api.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalStateException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalStateException("Error: Email is already in use!");
        }
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setProvider(AuthProvider.LOCAL);
        newUser.setRoles(Set.of("ROLE_USER"));
        return userRepository.save(newUser);
    }

    public Authentication loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );
        return authentication;
    }
}