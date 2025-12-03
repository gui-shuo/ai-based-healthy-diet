package com.nutriai.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 会员信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoResponse {
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 当前等级信息
     */
    private LevelInfo currentLevel;
    
    /**
     * 下一等级信息
     */
    private LevelInfo nextLevel;
    
    /**
     * 总成长值
     */
    private Integer totalGrowth;
    
    /**
     * 当前等级成长值
     */
    private Integer currentGrowth;
    
    /**
     * 升级进度百分比
     */
    private Double upgradeProgress;
    
    /**
     * 距离下一等级所需成长值
     */
    private Integer growthToNextLevel;
    
    /**
     * 邀请码
     */
    private String invitationCode;
    
    /**
     * 邀请链接
     */
    private String invitationLink;
    
    /**
     * 邀请人数
     */
    private Integer invitationCount;
    
    /**
     * 是否激活
     */
    private Boolean isActive;
    
    /**
     * 激活时间
     */
    private LocalDateTime activatedAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireAt;
    
    /**
     * 会员天数
     */
    private Long memberDays;
    
    /**
     * 等级信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelInfo {
        private Long levelId;
        private String levelCode;
        private String levelName;
        private Integer levelOrder;
        private Integer growthRequired;
        private Map<String, Object> benefits;
        private String iconUrl;
        private String color;
    }
}
