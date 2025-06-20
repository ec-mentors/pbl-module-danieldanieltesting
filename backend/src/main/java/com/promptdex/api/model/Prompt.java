package com.promptdex.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prompts")
@EqualsAndHashCode(exclude = "bookmarkedByUsers") // Prevent recursion
@ToString(exclude = "bookmarkedByUsers") // Prevent recursion
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_ai_model", nullable = false)
    private String targetAiModel;

    @Column(nullable = false)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

    // --- NEW RELATIONSHIP (INVERSE SIDE) ---
    @ManyToMany(mappedBy = "bookmarkedPrompts", fetch = FetchType.LAZY)
    private Set<User> bookmarkedByUsers = new HashSet<>();
}