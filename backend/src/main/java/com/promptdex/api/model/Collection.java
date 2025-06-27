package com.promptdex.api.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "collections")
@ToString(exclude = {"owner", "prompts"})
@EqualsAndHashCode(exclude = {"owner", "prompts"})
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "collection_prompts",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "prompt_id")
    )
    private Set<Prompt> prompts = new HashSet<>();
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    public Collection(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }
}