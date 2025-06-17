package com.promptdex.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data // Lombok: Generates getters, setters, equals, hashCode, and toString methods.
@NoArgsConstructor // Lombok: Generates a no-argument constructor.
@AllArgsConstructor // Lombok: Generates a constructor with all arguments.
@Entity // JPA: Marks this class as a database entity that can be persisted.
@Table(name = "users") // Specifies the exact table name in the database.
public class User {

    @Id // Marks this field as the primary key for the table.
    @GeneratedValue(strategy = GenerationType.UUID) // Configures the primary key to be a unique, auto-generated UUID.
    private UUID id;

    @Column(nullable = false, unique = true) // Column cannot be empty and must have a unique value.
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // This will store the securely hashed password.
}