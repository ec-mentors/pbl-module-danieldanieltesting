// src/main/java/com/promptdex/api/model/Prompt.java
package com.promptdex.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList; // <-- IMPORT
import java.util.List;      // <-- IMPORT
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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String targetAiModel;

    @Column(nullable = false)
    private String category;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // --- ADD THIS ENTIRE SECTION ---
    @OneToMany(
            mappedBy = "prompt", // "prompt" is the field name in the Review entity that links back to this Prompt
            cascade = CascadeType.ALL, // Operations (like delete) on a Prompt will cascade to its Reviews
            orphanRemoval = true,      // If a Review is removed from this list, it's deleted from the DB
            fetch = FetchType.LAZY     // Don't load reviews from the DB unless explicitly asked for
    )
    private List<Review> reviews = new ArrayList<>(); // Initialize to prevent NullPointerExceptions
}