package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe_ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String amount;

    @Column(length = 20)
    private String unit;

    @Column(name = "is_main")
    private Boolean isMain;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
