package com.nutriai.service;

import com.nutriai.entity.VipOrder;
import com.nutriai.entity.VipPlan;
import com.nutriai.repository.VipOrderRepository;
import com.nutriai.repository.VipPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会员权限服务
 * 基于 NutriAI 营养卡套餐管理功能权限与AI配额
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPermissionService {

    private final VipOrderRepository vipOrderRepository;
    private final VipPlanRepository vipPlanRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${nutriai.ai-quota.free:3}")
    private int freeQuota;

    private static final String QUOTA_KEY_PREFIX = "ai:quota:daily:";

    /**
     * 获取用户的有效会员等级（营养卡套餐代码）
     * 返回当前活跃VIP订单对应的套餐代码，无VIP则返回 FREE
     */
    public String getEffectiveTier(Long userId) {
        VipOrder activeOrder = getActiveOrder(userId);
        if (activeOrder == null) return "FREE";
        VipPlan plan = vipPlanRepository.findById(activeOrder.getPlanId()).orElse(null);
        if (plan == null) return "FREE";
        return plan.getPlanCode();
    }

    /**
     * 获取用户当前活跃的VIP订单（最新一笔）
     */
    private VipOrder getActiveOrder(Long userId) {
        List<VipOrder> activeOrders = vipOrderRepository.findActiveVipOrders(userId, LocalDateTime.now());
        return activeOrders.isEmpty() ? null : activeOrders.get(0);
    }

    /**
     * 检查用户VIP是否有效
     */
    public boolean isVipActive(Long userId) {
        return getActiveOrder(userId) != null;
    }

    /**
     * 获取VIP到期时间（无VIP返回null）
     */
    public LocalDateTime getVipExpireAt(Long userId) {
        VipOrder order = getActiveOrder(userId);
        return order != null ? order.getVipExpireAt() : null;
    }

    /**
     * 获取用户每日AI配额上限
     * 从营养卡套餐的 benefits JSON 中读取 ai_daily_quota，-1表示无限
     */
    public int getDailyQuota(Long userId) {
        VipOrder activeOrder = getActiveOrder(userId);
        if (activeOrder == null) return freeQuota;

        VipPlan plan = vipPlanRepository.findById(activeOrder.getPlanId()).orElse(null);
        if (plan == null) return freeQuota;

        // 从 benefits JSON 读取 ai_daily_quota
        if (plan.getBenefits() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> benefits = (Map<String, Object>) plan.getBenefits();
            Object quota = benefits.get("ai_daily_quota");
            if (quota instanceof Number) {
                int q = ((Number) quota).intValue();
                if (q == -1) return Integer.MAX_VALUE; // 无限
                return q;
            }
        }
        return freeQuota;
    }

    /**
     * 获取今日已使用AI次数
     */
    public int getTodayUsage(Long userId) {
        String key = QUOTA_KEY_PREFIX + LocalDate.now() + ":" + userId;
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Integer.parseInt(val) : 0;
    }

    /**
     * 检查用户是否还有AI额度
     */
    public boolean checkAiQuota(Long userId) {
        int quota = getDailyQuota(userId);
        if (quota == Integer.MAX_VALUE) return true;
        int used = getTodayUsage(userId);
        return used < quota;
    }

    /**
     * 消耗一次AI配额
     */
    public void consumeAiQuota(Long userId) {
        String key = QUOTA_KEY_PREFIX + LocalDate.now() + ":" + userId;
        Long newVal = redisTemplate.opsForValue().increment(key);
        if (newVal != null && newVal == 1) {
            redisTemplate.expire(key, Duration.ofHours(25));
        }
    }

    /**
     * 获取用户的权限概要（用于前端展示）
     */
    public Map<String, Object> getPermissionSummary(Long userId) {
        String tier = getEffectiveTier(userId);
        boolean isVip = isVipActive(userId);
        int quota = getDailyQuota(userId);
        int used = getTodayUsage(userId);
        LocalDateTime vipExpireAt = getVipExpireAt(userId);

        String planName = "免费用户";
        if (isVip) {
            VipOrder order = getActiveOrder(userId);
            if (order != null) planName = order.getPlanName();
        }

        return Map.of(
                "tier", tier,
                "isVip", isVip,
                "planName", planName,
                "aiQuotaTotal", quota == Integer.MAX_VALUE ? -1 : quota,
                "aiQuotaUsed", used,
                "aiQuotaRemain", quota == Integer.MAX_VALUE ? -1 : Math.max(quota - used, 0),
                "vipExpireAt", vipExpireAt != null ? vipExpireAt.toString() : "",
                "features", getFeaturesByPlan(tier)
        );
    }

    /**
     * 根据套餐获取可用功能
     */
    private Map<String, Boolean> getFeaturesByPlan(String planCode) {
        boolean isVip = !"FREE".equals(planCode);
        boolean isPremium = List.of("QUARTERLY", "YEARLY", "LIFETIME").contains(planCode);
        return Map.of(
                "aiChat", true,
                "foodRecognition", true,
                "dietPlan", isVip,
                "imageRecognition", true,
                "unlimitedRecords", isPremium,
                "prioritySupport", isPremium
        );
    }
}
