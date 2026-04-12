package com.nutriai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_corpus")
public class RecipeCorpus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(length = 200)
    private String dish;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "ingredients_json", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String ingredientsJson;

    @Column(name = "steps_json", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String stepsJson;

    @Column(name = "keywords_json", length = 1000)
    private String keywordsJson;

    @Column(length = 30)
    private String category;

    @Column(length = 100)
    private String author;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDish() { return dish; }
    public void setDish(String dish) { this.dish = dish; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredientsJson() { return ingredientsJson; }
    public void setIngredientsJson(String ingredientsJson) { this.ingredientsJson = ingredientsJson; }

    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }

    public String getKeywordsJson() { return keywordsJson; }
    public void setKeywordsJson(String keywordsJson) { this.keywordsJson = keywordsJson; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
