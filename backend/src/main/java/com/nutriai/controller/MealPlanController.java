package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.entity.*;
import com.nutriai.service.MealPlanService;
import com.nutriai.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询健康餐列表（公开）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MealPlan>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String dietGoal,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort) {
        try {
            Page<MealPlan> plans = mealPlanService.getMealPlans(page, size, dietGoal, planType, keyword, sort);
            return ResponseEntity.ok(ApiResponse.success("获取成功", plans));
        } catch (Exception e) {
            log.error("获取健康餐列表失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取失败"));
        }
    }

    /**
     * 获取精选健康餐（公开）
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<MealPlan>>> featured() {
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getFeaturedMealPlans()));
    }

    /**
     * 健康餐详情（公开，含收藏状态）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detail(@PathVariable Long id, HttpServletRequest request) {
        try {
            MealPlan plan = mealPlanService.getMealPlanDetail(id);
            if (plan == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "健康餐不存在"));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("mealPlan", plan);
            Long userId = extractUserIdSilently(request);
            result.put("isFavorited", userId != null && mealPlanService.isFavorited(userId, id));
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            log.error("获取健康餐详情失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取失败"));
        }
    }

    /**
     * 收藏/取消收藏（需登录）
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        boolean favorited = mealPlanService.toggleFavorite(userId, id);
        return ResponseEntity.ok(ApiResponse.success(favorited ? "已收藏" : "已取消收藏",
                Map.of("favorited", favorited)));
    }

    /**
     * 我的收藏健康餐（需登录）
     */
    @GetMapping("/my-favorites")
    public ResponseEntity<ApiResponse<Page<MealPlanFavorite>>> myFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getUserFavorites(userId, page, size)));
    }

    private Long extractUserId(HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return jwtUtil.getUserIdFromToken(header.substring(7));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Long extractUserIdSilently(HttpServletRequest request) {
        return extractUserId(request);
    }
}
