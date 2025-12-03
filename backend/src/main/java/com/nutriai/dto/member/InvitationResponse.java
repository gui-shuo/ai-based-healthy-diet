package com.nutriai.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邀请记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    
    /**
     * 邀请ID
     */
    private Long id;
    
    /**
     * 邀请人ID
     */
    private Long inviterId;
    
    /**
     * 邀请人用户名
     */
    private String inviterName;
    
    /**
     * 被邀请人ID
     */
    private Long inviteeId;
    
    /**
     * 被邀请人用户名
     */
    private String inviteeName;
    
    /**
     * 邀请码
     */
    private String invitationCode;
    
    /**
     * 状态：PENDING, ACCEPTED, EXPIRED
     */
    private String status;
    
    /**
     * 状态名称
     */
    private String statusName;
    
    /**
     * 邀请时间
     */
    private LocalDateTime invitedAt;
    
    /**
     * 接受时间
     */
    private LocalDateTime acceptedAt;
    
    /**
     * 奖励成长值
     */
    private Integer rewardGrowth;
    
    /**
     * 是否已发放奖励
     */
    private Boolean isRewarded;
}
