package com.promptdex.api.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tags")
@EqualsAndHashCode(exclude = "prompts") 
@ToString(exclude = "prompts") 
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Prompt> prompts = new HashSet<>();
}