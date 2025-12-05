package com.nutriai.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 饮食计划生成请求
 */
@Data
public class DietPlanRequest {
    
    /**
     * 计划天数（7天或30天）
     */
    @NotNull(message = "计划天数不能为空")
    @Min(value = 1, message = "计划天数至少为1天")
    @Max(value = 30, message = "计划天数最多为30天")
    private Integer days;
    
    /**
     * 目标：lose_weight（减脂）、gain_muscle（增肌）、maintain（维持健康）
     */
    @NotNull(message = "目标不能为空")
    private String goal;
    
    /**
     * 运动强度：low（轻度）、medium（中度）、high（高强度）
     */
    private String exerciseLevel;
    
    /**
     * 饮食偏好/忌口
     */
    private String preferences;
    
    /**
     * 过敏食物
     */
    private String allergies;
    
    /**
     * 每日热量目标（可选，如果不提供则AI自动计算）
     */
    private Integer dailyCalories;
    
    /**
     * 用户身高（cm）
     */
    private Double height;
    
    /**
     * 用户体重（kg）
     */
    private Double weight;
    
    /**
     * 用户年龄
     */
    private Integer age;
    
    /**
     * 性别：male（男）、female（女）
     */
    private String gender;
    
    /**
     * 是否包含详细营养数据
     */
    private Boolean includeNutrition = true;
    
    /**
     * 是否生成采购清单
     */
    private Boolean includeShoppingList = true;
    
    /**
     * 是否包含烹饪提示
     */
    private Boolean includeCookingTips = true;
}
