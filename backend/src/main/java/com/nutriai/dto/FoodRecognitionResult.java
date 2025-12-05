package com.nutriai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 食物识别结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRecognitionResult {
    
    /**
     * 识别到的食物列表
     */
    private List<FoodItem> foods;
    
    /**
     * 识别总数
     */
    private Integer totalCount;
    
    /**
     * 识别时间（毫秒）
     */
    private Long recognitionTime;
    
    /**
     * 图片URL
     */
    private String imageUrl;
}
