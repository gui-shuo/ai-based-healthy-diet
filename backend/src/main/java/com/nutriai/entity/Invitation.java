package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邀请记录实体类
 */
@Entity
@Table(name = "invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 邀请人ID
     */
    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;
    
    /**
     * 被邀请人ID
     */
    @Column(name = "invitee_id")
    private Long inviteeId;
    
    /**
     * 邀请码
     */
    @Column(name = "invitation_code", nullable = false, length = 20)
    private String invitationCode;
    
    /**
     * 状态：PENDING, ACCEPTED, EXPIRED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";
    
    /**
     * 邀请时间
     */
    @Column(name = "invited_at", nullable = false, updatable = false)
    private LocalDateTime invitedAt;
    
    /**
     * 接受时间
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    /**
     * 奖励成长值
     */
    @Column(name = "reward_growth", nullable = false)
    @Builder.Default
    private Integer rewardGrowth = 0;
    
    /**
     * 是否已发放奖励
     */
    @Column(name = "is_rewarded", nullable = false)
    @Builder.Default
    private Boolean isRewarded = false;
    
    @PrePersist
    protected void onCreate() {
        if (invitedAt == null) {
            invitedAt = LocalDateTime.now();
        }
    }
}
