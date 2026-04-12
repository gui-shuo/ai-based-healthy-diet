package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plan_favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "meal_plan_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "meal_plan_id", nullable = false)
    private Long mealPlanId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
