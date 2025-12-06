package com.nutriai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI聊天日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatLogDTO {
    private Long id;
    private Long userId;
    private String username;            // 用户名（关联查询）
    private String sessionId;
    private String userMessage;
    private String aiResponse;
    private String model;
    private Integer tokensUsed;
    private Integer responseTime;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
