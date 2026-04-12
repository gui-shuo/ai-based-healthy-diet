package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plan_checkins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follow_id", "day_number", "meal_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follow_id", nullable = false)
    private Long followId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(length = 500)
    private String note;

    @PrePersist
    protected void onCreate() {
        if (checkedAt == null) checkedAt = LocalDateTime.now();
    }
}
