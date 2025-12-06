package com.nutriai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI聊天历史记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatHistoryDTO {
    private Long id;
    private String title;
    private String messages;  // JSON字符串
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
