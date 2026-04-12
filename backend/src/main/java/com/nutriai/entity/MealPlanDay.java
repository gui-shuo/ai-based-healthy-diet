package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "meal_plan_days")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_plan_id", nullable = false)
    private Long mealPlanId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(length = 100)
    private String title;

    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "total_protein", precision = 6, scale = 1)
    private BigDecimal totalProtein;

    @Column(name = "total_fat", precision = 6, scale = 1)
    private BigDecimal totalFat;

    @Column(name = "total_carbs", precision = 6, scale = 1)
    private BigDecimal totalCarbs;

    @OneToMany(mappedBy = "mealPlanDayId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("mealType ASC, sortOrder ASC")
    private List<MealPlanItem> items;
}
