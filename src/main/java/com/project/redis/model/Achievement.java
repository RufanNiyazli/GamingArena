package com.project.redis.model;

import com.project.redis.enums.AchievementRarity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements", indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_points", columnList = "points")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String icon;

    @Column(nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AchievementRarity rarity = AchievementRarity.COMMON;

    @Column(nullable = false)
    private Integer points;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @ManyToMany(mappedBy = "achievements")
    @Builder.Default
    private List<User> users = new ArrayList<>();
}
