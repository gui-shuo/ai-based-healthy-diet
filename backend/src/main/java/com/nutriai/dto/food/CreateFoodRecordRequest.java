package com.nutriai.dto.food;

import com.nutriai.entity.FoodRecord;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建饮食记录请求
 */
@Data
public class CreateFoodRecordRequest {
    
    /**
     * 餐次类型
     */
    @NotNull(message = "餐次类型不能为空")
    private FoodRecord.MealType mealType;
    
    /**
     * 食物名称
     */
    @NotBlank(message = "食物名称不能为空")
    @Size(max = 100, message = "食物名称最多100个字符")
    private String foodName;
    
    /**
     * 食物照片URL
     */
    @Size(max = 500, message = "照片URL最多500个字符")
    private String photoUrl;
    
    /**
     * 份量（克）
     */
    @DecimalMin(value = "0.01", message = "份量必须大于0")
    @Digits(integer = 6, fraction = 2, message = "份量格式不正确")
    private BigDecimal portion;
    
    /**
     * 卡路里（千卡）
     */
    @DecimalMin(value = "0", message = "卡路里不能为负数")
    @Digits(integer = 6, fraction = 2, message = "卡路里格式不正确")
    private BigDecimal calories;
    
    /**
     * 蛋白质（克）
     */
    @DecimalMin(value = "0", message = "蛋白质不能为负数")
    @Digits(integer = 6, fraction = 2, message = "蛋白质格式不正确")
    private BigDecimal protein;
    
    /**
     * 碳水化合物（克）
     */
    @DecimalMin(value = "0", message = "碳水化合物不能为负数")
    @Digits(integer = 6, fraction = 2, message = "碳水化合物格式不正确")
    private BigDecimal carbohydrates;
    
    /**
     * 脂肪（克）
     */
    @DecimalMin(value = "0", message = "脂肪不能为负数")
    @Digits(integer = 6, fraction = 2, message = "脂肪格式不正确")
    private BigDecimal fat;
    
    /**
     * 纤维（克）
     */
    @DecimalMin(value = "0", message = "纤维不能为负数")
    @Digits(integer = 6, fraction = 2, message = "纤维格式不正确")
    private BigDecimal fiber;
    
    /**
     * 记录日期时间
     */
    @NotNull(message = "记录时间不能为空")
    private LocalDateTime recordTime;
    
    /**
     * 备注
     */
    @Size(max = 500, message = "备注最多500个字符")
    private String notes;
}
