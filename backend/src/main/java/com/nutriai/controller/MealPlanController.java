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

    // ========== Browse & Discovery ==========

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
            log.error("获取营养餐列表失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取失败"));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<MealPlan>>> featured() {
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getFeaturedMealPlans()));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<String>>> tags() {
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getAllTags()));
    }

    @GetMapping("/by-tag")
    public ResponseEntity<ApiResponse<Page<MealPlan>>> byTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.searchByTag(tag, page, size)));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<MealPlan>>> recommendations(HttpServletRequest request) {
        Long userId = extractUserIdSilently(request);
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getRecommendations(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detail(@PathVariable Long id, HttpServletRequest request) {
        try {
            MealPlan plan = mealPlanService.getMealPlanDetail(id);
            if (plan == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "营养餐不存在"));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("mealPlan", plan);
            Long userId = extractUserIdSilently(request);
            result.put("isFavorited", userId != null && mealPlanService.isFavorited(userId, id));
            if (userId != null) {
                MealPlanFollow follow = mealPlanService.getFollowStatus(userId, id);
                result.put("followStatus", follow);
                result.put("isFollowing", follow != null && "ACTIVE".equals(follow.getStatus()));
                MealPlanRating rating = mealPlanService.getUserRating(userId, id);
                result.put("userRating", rating);
            } else {
                result.put("isFollowing", false);
            }
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            log.error("获取营养餐详情失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取失败"));
        }
    }

    // ========== Favorites ==========

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        boolean favorited = mealPlanService.toggleFavorite(userId, id);
        return ResponseEntity.ok(ApiResponse.success(favorited ? "已收藏" : "已取消收藏",
                Map.of("favorited", favorited)));
    }

    @GetMapping("/my-favorites")
    public ResponseEntity<ApiResponse<Page<MealPlanFavorite>>> myFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getUserFavorites(userId, page, size)));
    }

    // ========== Follow & Track ==========

    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<MealPlanFollow>> follow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        try {
            MealPlanFollow follow = mealPlanService.followPlan(userId, id);
            return ResponseEntity.ok(ApiResponse.success("已开始跟随营养餐计划", follow));
        } catch (Exception e) {
            log.error("跟随营养餐失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "操作失败"));
        }
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        mealPlanService.unfollowPlan(userId, id);
        return ResponseEntity.ok(ApiResponse.success("已取消跟随", null));
    }

    @GetMapping("/my-follows")
    public ResponseEntity<ApiResponse<List<MealPlanFollow>>> myFollows(HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getActiveFollows(userId)));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> progress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        Map<String, Object> progress = mealPlanService.getFollowProgress(userId, id);
        if (progress == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "未跟随此营养餐"));
        }
        return ResponseEntity.ok(ApiResponse.success("获取成功", progress));
    }

    // ========== Check-in ==========

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<MealPlanCheckin>> checkin(
            @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        try {
            Long followId = Long.valueOf(body.get("followId").toString());
            int dayNumber = Integer.parseInt(body.get("dayNumber").toString());
            String mealType = body.get("mealType").toString();
            MealPlanCheckin checkin = mealPlanService.checkin(userId, followId, dayNumber, mealType);
            return ResponseEntity.ok(ApiResponse.success("打卡成功 ✅", checkin));
        } catch (Exception e) {
            log.error("打卡失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, e.getMessage()));
        }
    }

    @DeleteMapping("/checkin")
    public ResponseEntity<ApiResponse<Void>> uncheckin(
            @RequestParam Long followId, @RequestParam int dayNumber,
            @RequestParam String mealType, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        mealPlanService.uncheckin(userId, followId, dayNumber, mealType);
        return ResponseEntity.ok(ApiResponse.success("已取消打卡", null));
    }

    // ========== Ratings ==========

    @PostMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<MealPlanRating>> rate(
            @PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        try {
            int rating = Integer.parseInt(body.get("rating").toString());
            String review = body.get("review") != null ? body.get("review").toString() : null;
            MealPlanRating r = mealPlanService.ratePlan(userId, id, rating, review);
            return ResponseEntity.ok(ApiResponse.success("评价成功", r));
        } catch (Exception e) {
            log.error("评价失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, e.getMessage()));
        }
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<ApiResponse<Page<MealPlanRating>>> ratings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("获取成功", mealPlanService.getPlanRatings(id, page, size)));
    }

    // ========== Helpers ==========

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
