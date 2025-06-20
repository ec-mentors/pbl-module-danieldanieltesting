// src/main/java/com/promptdex/api/model/User.java
package com.promptdex.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(exclude = {"bookmarkedPrompts", "collections"}) // <-- UPDATED
@ToString(exclude = {"bookmarkedPrompts", "collections"}) // <-- UPDATED
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'LOCAL'")
    private AuthProvider provider;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_bookmarks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "prompt_id")
    )
    private Set<Prompt> bookmarkedPrompts = new HashSet<>();

    // --- NEW RELATIONSHIP TO COLLECTIONS ---
    @OneToMany(
            mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Collection> collections = new HashSet<>();
}