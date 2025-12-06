package com.nutriai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDTO {
    private String date;    // 日期
    private Long count;     // 数量
    private Long success;   // 成功数（用于AI调用统计）
}
