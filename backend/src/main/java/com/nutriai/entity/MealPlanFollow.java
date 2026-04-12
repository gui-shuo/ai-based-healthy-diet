package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plan_follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "meal_plan_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "meal_plan_id", nullable = false)
    private Long mealPlanId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "current_day")
    private Integer currentDay;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private MealPlan mealPlan;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentDay == null) currentDay = 1;
        if (status == null) status = "ACTIVE";
        if (startDate == null) startDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
