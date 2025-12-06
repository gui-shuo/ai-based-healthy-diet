package com.nutriai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI聊天收藏DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatFavoriteDTO {
    private Long id;
    private String messageContent;
    private String messageRole;
    private LocalDateTime createdAt;
}
