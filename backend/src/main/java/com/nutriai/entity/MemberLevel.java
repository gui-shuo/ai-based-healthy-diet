package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 会员等级配置实体类
 */
@Entity
@Table(name = "member_levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 等级代码（ROOKIE, BRONZE, SILVER, GOLD, PLATINUM）
     */
    @Column(name = "level_code", nullable = false, unique = true, length = 20)
    private String levelCode;
    
    /**
     * 等级名称
     */
    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;
    
    /**
     * 等级顺序
     */
    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;
    
    /**
     * 所需成长值
     */
    @Column(name = "growth_required", nullable = false)
    private Integer growthRequired;
    
    /**
     * 权益配置（JSON格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "benefits", columnDefinition = "json")
    private Map<String, Object> benefits;
    
    /**
     * 等级图标URL
     */
    @Column(name = "icon_url")
    private String iconUrl;
    
    /**
     * 等级颜色
     */
    @Column(name = "color", length = 20)
    private String color;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
