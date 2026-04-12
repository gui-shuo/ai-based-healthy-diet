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
@Table(name = "meal_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "plan_type", length = 20)
    private String planType;

    @Column(name = "diet_goal", length = 30)
    private String dietGoal;

    @Column(name = "target_calories")
    private Integer targetCalories;

    @Column(name = "target_protein", precision = 6, scale = 1)
    private BigDecimal targetProtein;

    @Column(name = "target_fat", precision = 6, scale = 1)
    private BigDecimal targetFat;

    @Column(name = "target_carbs", precision = 6, scale = 1)
    private BigDecimal targetCarbs;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "suitable_crowd", length = 200)
    private String suitableCrowd;

    @Column(length = 500)
    private String tags;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    private java.math.BigDecimal avgRating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "follow_count")
    private Integer followCount;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "favorite_count")
    private Integer favoriteCount;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "mealPlanId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    private List<MealPlanDay> days;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isFeatured == null) isFeatured = false;
        if (viewCount == null) viewCount = 0;
        if (favoriteCount == null) favoriteCount = 0;
        if (followCount == null) followCount = 0;
        if (ratingCount == null) ratingCount = 0;
        if (avgRating == null) avgRating = java.math.BigDecimal.ZERO;
        if (difficulty == null) difficulty = "MEDIUM";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
