// src/main/java/com/promptdex/api/dto/RegisterRequest.java
package com.promptdex.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// <-- CHANGE HERE: Annotations moved to the class level
@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String username; // Lombok will generate getUsername() & setUsername()

    @NotBlank
    private String password; // Lombok will generate getPassword() & setPassword()

    @NotBlank
    @Email
    private String email;    // Lombok will generate getEmail() & setEmail()
}