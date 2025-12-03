package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成长值记录实体类
 */
@Entity
@Table(name = "growth_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 会员ID
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 成长值
     */
    @Column(name = "growth_value", nullable = false)
    private Integer growthValue;
    
    /**
     * 成长值类型
     */
    @Column(name = "growth_type", nullable = false, length = 50)
    private String growthType;
    
    /**
     * 描述
     */
    @Column(name = "description")
    private String description;
    
    /**
     * 关联业务ID
     */
    @Column(name = "related_id")
    private Long relatedId;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
