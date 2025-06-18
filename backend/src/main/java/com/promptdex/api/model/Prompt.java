// src/main/java/com/promptdex/api/model/Prompt.java
package com.promptdex.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prompts")
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    // --- MODIFICATION START ---
    // Explicitly mapping column names to avoid any naming strategy ambiguity.
    // By convention, Spring JPA maps camelCase (promptText) to snake_case (prompt_text).
    // Let's make this explicit for clarity and safety.

    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_ai_model", nullable = false)
    private String targetAiModel;
    // --- MODIFICATION END ---

    @Column(nullable = false)
    private String category;

    // --- CRITICAL FIX START ---
    // Here we explicitly define the database column names for our timestamps.
    // This is the most likely source of the error.
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    // --- CRITICAL FIX END ---


    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(
            mappedBy = "prompt",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Review> reviews = new ArrayList<>();
}