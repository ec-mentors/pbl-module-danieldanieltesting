package com.promptdex.api.controller;

import com.promptdex.api.dto.AuthResponse;
import com.promptdex.api.dto.LoginRequest;
import com.promptdex.api.dto.RegisterRequest;
import com.promptdex.api.security.JwtTokenProvider;
import com.promptdex.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Call the authService to get the authenticated Authentication object
        Authentication authentication = authService.loginUser(loginRequest);

        // 2. Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Pass the object to the token provider to generate the JWT string
        String jwt = tokenProvider.generateToken(authentication);

        // 4. Return a ResponseEntity.ok() with the AuthResponse DTO
        return ResponseEntity.ok(new AuthResponse(jwt, authentication.getName()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Call the authService to handle registration logic
        authService.registerUser(registerRequest);

        // Return a 201 Created response with a success message
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }
}