package com.nutriai.dto.food;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 营养摄入统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionStatsResponse {
    
    /**
     * 统计日期
     */
    private LocalDate date;
    
    /**
     * 总卡路里
     */
    private BigDecimal totalCalories;
    
    /**
     * 总蛋白质
     */
    private BigDecimal totalProtein;
    
    /**
     * 总碳水化合物
     */
    private BigDecimal totalCarbohydrates;
    
    /**
     * 总脂肪
     */
    private BigDecimal totalFat;
    
    /**
     * 总纤维
     */
    private BigDecimal totalFiber;
    
    /**
     * 早餐卡路里
     */
    private BigDecimal breakfastCalories;
    
    /**
     * 午餐卡路里
     */
    private BigDecimal lunchCalories;
    
    /**
     * 晚餐卡路里
     */
    private BigDecimal dinnerCalories;
    
    /**
     * 加餐卡路里
     */
    private BigDecimal snackCalories;
    
    /**
     * 记录数量
     */
    private Integer recordCount;
    
    /**
     * 各餐次记录数
     */
    private MealStats mealStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealStats {
        private Integer breakfastCount;
        private Integer lunchCount;
        private Integer dinnerCount;
        private Integer snackCount;
    }
}
