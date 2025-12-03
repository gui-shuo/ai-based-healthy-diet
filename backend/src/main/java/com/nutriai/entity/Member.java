package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员信息实体类
 */
@Entity
@Table(name = "members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    /**
     * 当前等级ID
     */
    @Column(name = "level_id", nullable = false)
    private Long levelId;
    
    /**
     * 总成长值
     */
    @Column(name = "total_growth", nullable = false)
    @Builder.Default
    private Integer totalGrowth = 0;
    
    /**
     * 当前等级成长值
     */
    @Column(name = "current_growth", nullable = false)
    @Builder.Default
    private Integer currentGrowth = 0;
    
    /**
     * 邀请码
     */
    @Column(name = "invitation_code", nullable = false, unique = true, length = 20)
    private String invitationCode;
    
    /**
     * 邀请人ID
     */
    @Column(name = "invited_by")
    private Long invitedBy;
    
    /**
     * 邀请人数
     */
    @Column(name = "invitation_count", nullable = false)
    @Builder.Default
    private Integer invitationCount = 0;
    
    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * 激活时间
     */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    /**
     * 过期时间
     */
    @Column(name = "expire_at")
    private LocalDateTime expireAt;
    
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
        if (activatedAt == null) {
            activatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
