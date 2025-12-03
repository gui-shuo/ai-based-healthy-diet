package com.nutriai.dto.food;

import com.nutriai.entity.FoodRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRecordResponse {
    
    private Long id;
    private Long userId;
    private FoodRecord.MealType mealType;
    private String mealTypeName;
    private String foodName;
    private String photoUrl;
    private BigDecimal portion;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrates;
    private BigDecimal fat;
    private BigDecimal fiber;
    private LocalDateTime recordTime;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为响应DTO
     */
    public static FoodRecordResponse from(FoodRecord record) {
        return FoodRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .mealType(record.getMealType())
                .mealTypeName(record.getMealType().getDisplayName())
                .foodName(record.getFoodName())
                .photoUrl(record.getPhotoUrl())
                .portion(record.getPortion())
                .calories(record.getCalories())
                .protein(record.getProtein())
                .carbohydrates(record.getCarbohydrates())
                .fat(record.getFat())
                .fiber(record.getFiber())
                .recordTime(record.getRecordTime())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
