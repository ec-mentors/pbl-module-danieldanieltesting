package com.promptdex.api.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.time.Instant;
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
@EqualsAndHashCode(exclude = {"bookmarkedByUsers", "tags", "collections", "reviews"}) 
@ToString(exclude = {"bookmarkedByUsers", "tags", "collections", "reviews"}) 
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
    @Column(name = "average_rating") 
    private Double averageRating;
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
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
    @ManyToMany(mappedBy = "bookmarkedPrompts", fetch = FetchType.LAZY)
    private Set<User> bookmarkedByUsers = new HashSet<>();
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "prompt_tags",
            joinColumns = @JoinColumn(name = "prompt_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    @ManyToMany(mappedBy = "prompts", fetch = FetchType.LAZY)
    private Set<Collection> collections = new HashSet<>();
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    @PreRemove
    private void preRemoveCleanup() {
        for (Collection collection : new HashSet<>(this.collections)) { 
            collection.getPrompts().remove(this);
        }
        this.collections.clear();
        for (User user : new HashSet<>(this.bookmarkedByUsers)) { 
            user.getBookmarkedPrompts().remove(this);
        }
        this.bookmarkedByUsers.clear();
    }
}