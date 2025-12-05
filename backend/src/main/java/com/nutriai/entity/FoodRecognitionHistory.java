package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 食物识别历史记录
 */
@Entity
@Table(name = "food_recognition_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRecognitionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 识别方式：TEXT-文本识别, IMAGE-图片识别
     */
    @Column(name = "recognition_type", length = 20)
    private String recognitionType;
    
    /**
     * 输入的文本内容（文本识别时使用）
     */
    @Column(name = "input_text", length = 200)
    private String inputText;
    
    /**
     * 图片URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    /**
     * 识别结果（JSON格式）
     */
    @Column(name = "recognition_result", columnDefinition = "JSON")
    private String recognitionResult;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
