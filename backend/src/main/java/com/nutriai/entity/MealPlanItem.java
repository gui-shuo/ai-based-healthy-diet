package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "meal_plan_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_plan_day_id", nullable = false)
    private Long mealPlanDayId;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "recipe_corpus_id")
    private Long recipeCorpusId;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String portion;

    private Integer calories;

    @Column(precision = 6, scale = 1)
    private BigDecimal protein;

    @Column(precision = 6, scale = 1)
    private BigDecimal fat;

    @Column(precision = 6, scale = 1)
    private BigDecimal carbs;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
