package com.nutriai.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成长值记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecordResponse {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 成长值（正数=增加，负数=减少）
     */
    private Integer growthValue;
    
    /**
     * 成长值类型
     */
    private String growthType;
    
    /**
     * 成长值类型名称
     */
    private String growthTypeName;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 关联业务ID
     */
    private Long relatedId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 剩余成长值（该记录后的总成长值）
     */
    private Integer remainingGrowth;
}
