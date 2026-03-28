package com.nutriai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据看板统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    private UserStats userStats;
    private ChatStats chatStats;
    private AIStats aiStats;
    private MemberStats memberStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Long totalUsers;        // 总用户数
        private Long todayNewUsers;     // 今日新增用户
        private Long activeUsers;       // 活跃用户（7天内有活动）
        private Long yesterdayNewUsers; // 昨日新增
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatStats {
        private Long totalChats;        // 总对话数
        private Long todayChats;        // 今日对话数
        private Double avgResponseTime; // 平均响应时间(ms)
        private Long yesterdayChats;    // 昨日对话数
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIStats {
        private Long totalCalls;        // 总调用次数
        private Double successRate;     // 成功率(%)
        private Double avgTokens;       // 平均token使用量
        private Long todayCalls;        // 今日调用次数
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberStats {
        private Long free;              // 免费用户数
        private Long vip;               // VIP会员数（所有营养卡用户）
    }
}
