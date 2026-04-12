package com.nutriai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(length = 30, nullable = false)
    private String category;

    @Column(name = "cuisine_type", length = 30)
    private String cuisineType;

    @Column(name = "diet_type", length = 30)
    private String dietType;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "cooking_time_minutes")
    private Integer cookingTimeMinutes;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    private Integer servings;

    @Column(name = "calories_per_serving")
    private Integer caloriesPerServing;

    @Column(name = "protein_per_serving", precision = 6, scale = 1)
    private BigDecimal proteinPerServing;

    @Column(name = "fat_per_serving", precision = 6, scale = 1)
    private BigDecimal fatPerServing;

    @Column(name = "carbs_per_serving", precision = 6, scale = 1)
    private BigDecimal carbsPerServing;

    @Column(name = "fiber_per_serving", precision = 6, scale = 1)
    private BigDecimal fiberPerServing;

    @Column(columnDefinition = "TEXT")
    private String tips;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "favorite_count")
    private Integer favoriteCount;

    @Column(name = "rating_avg", precision = 2, scale = 1)
    private BigDecimal ratingAvg;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recipeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<RecipeIngredient> ingredients;

    @OneToMany(mappedBy = "recipeId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<RecipeStep> steps;

    @OneToMany(mappedBy = "recipeId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeTag> tags;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isFeatured == null) isFeatured = false;
        if (viewCount == null) viewCount = 0;
        if (favoriteCount == null) favoriteCount = 0;
        if (ratingCount == null) ratingCount = 0;
        if (ratingAvg == null) ratingAvg = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
