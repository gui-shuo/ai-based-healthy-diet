package com.nutriai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "food_nutrition_kb")
public class FoodNutritionKb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Column(length = 300)
    private String alias;

    @Column(name = "english_name", length = 300)
    private String englishName;

    @Column(name = "edible_portion_pct", length = 20)
    private String ediblePortionPct;

    @Column(name = "water_g", length = 20)
    private String waterG;

    @Column(name = "energy_kcal", length = 20)
    private String energyKcal;

    @Column(name = "energy_kj", length = 20)
    private String energyKj;

    @Column(name = "protein_g", length = 20)
    private String proteinG;

    @Column(name = "fat_g", length = 20)
    private String fatG;

    @Column(name = "carbohydrate_g", length = 20)
    private String carbohydrateG;

    @Column(name = "dietary_fiber_g", length = 20)
    private String dietaryFiberG;

    @Column(name = "cholesterol_mg", length = 20)
    private String cholesterolMg;

    @Column(name = "vitamin_a", length = 20)
    private String vitaminA;

    @Column(name = "vitamin_c", length = 20)
    private String vitaminC;

    @Column(name = "calcium_mg", length = 20)
    private String calciumMg;

    @Column(name = "iron_mg", length = 20)
    private String ironMg;

    @Column(name = "zinc_mg", length = 20)
    private String zincMg;

    @Column(name = "sodium_mg", length = 20)
    private String sodiumMg;

    @Column(name = "potassium_mg", length = 20)
    private String potassiumMg;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public String getEnglishName() { return englishName; }
    public void setEnglishName(String englishName) { this.englishName = englishName; }
    public String getEdiblePortionPct() { return ediblePortionPct; }
    public void setEdiblePortionPct(String ediblePortionPct) { this.ediblePortionPct = ediblePortionPct; }
    public String getWaterG() { return waterG; }
    public void setWaterG(String waterG) { this.waterG = waterG; }
    public String getEnergyKcal() { return energyKcal; }
    public void setEnergyKcal(String energyKcal) { this.energyKcal = energyKcal; }
    public String getEnergyKj() { return energyKj; }
    public void setEnergyKj(String energyKj) { this.energyKj = energyKj; }
    public String getProteinG() { return proteinG; }
    public void setProteinG(String proteinG) { this.proteinG = proteinG; }
    public String getFatG() { return fatG; }
    public void setFatG(String fatG) { this.fatG = fatG; }
    public String getCarbohydrateG() { return carbohydrateG; }
    public void setCarbohydrateG(String carbohydrateG) { this.carbohydrateG = carbohydrateG; }
    public String getDietaryFiberG() { return dietaryFiberG; }
    public void setDietaryFiberG(String dietaryFiberG) { this.dietaryFiberG = dietaryFiberG; }
    public String getCholesterolMg() { return cholesterolMg; }
    public void setCholesterolMg(String cholesterolMg) { this.cholesterolMg = cholesterolMg; }
    public String getVitaminA() { return vitaminA; }
    public void setVitaminA(String vitaminA) { this.vitaminA = vitaminA; }
    public String getVitaminC() { return vitaminC; }
    public void setVitaminC(String vitaminC) { this.vitaminC = vitaminC; }
    public String getCalciumMg() { return calciumMg; }
    public void setCalciumMg(String calciumMg) { this.calciumMg = calciumMg; }
    public String getIronMg() { return ironMg; }
    public void setIronMg(String ironMg) { this.ironMg = ironMg; }
    public String getZincMg() { return zincMg; }
    public void setZincMg(String zincMg) { this.zincMg = zincMg; }
    public String getSodiumMg() { return sodiumMg; }
    public void setSodiumMg(String sodiumMg) { this.sodiumMg = sodiumMg; }
    public String getPotassiumMg() { return potassiumMg; }
    public void setPotassiumMg(String potassiumMg) { this.potassiumMg = potassiumMg; }

    public String toNutritionSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(foodName);
        if (alias != null && !alias.isEmpty()) sb.append("(").append(alias).append(")");
        sb.append(": ");
        if (energyKcal != null && !energyKcal.isEmpty()) sb.append("能量").append(energyKcal).append(", ");
        if (proteinG != null && !proteinG.isEmpty()) sb.append("蛋白质").append(proteinG).append(", ");
        if (fatG != null && !fatG.isEmpty()) sb.append("脂肪").append(fatG).append(", ");
        if (carbohydrateG != null && !carbohydrateG.isEmpty()) sb.append("碳水").append(carbohydrateG).append(", ");
        if (dietaryFiberG != null && !dietaryFiberG.isEmpty()) sb.append("膳食纤维").append(dietaryFiberG);
        return sb.toString().replaceAll(",\\s*$", "");
    }
}
