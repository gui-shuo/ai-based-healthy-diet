package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.entity.*;
import com.nutriai.service.RecipeService;
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
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询食谱列表（公开）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Recipe>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dietType,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String sort) {
        try {
            Page<Recipe> recipes = recipeService.getRecipes(page, size, category, dietType, cuisineType, difficulty, keyword, tag, sort);
            return ResponseEntity.ok(ApiResponse.success("获取成功", recipes));
        } catch (Exception e) {
            log.error("获取食谱列表失败", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "获取失败"));
        }
    }

    /**
     * 获取精选食谱（公开）
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Recipe>>> featured() {
        return ResponseEntity.ok(ApiResponse.success("获取成功", recipeService.getFeaturedRecipes()));
    }

    /**
     * 获取所有标签（公开）
     */
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<String>>> tags() {
        return ResponseEntity.ok(ApiResponse.success("获取成功", recipeService.getAllTags()));
    }

    /**
     * 食谱详情（公开，含收藏状态）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Recipe recipe = recipeService.getRecipeDetail(id);
            if (recipe == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "食谱不存在"));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("recipe", recipe);
            Long userId = extractUserIdSilently(request);
            result.put("isFavorited", userId != null && recipeService.isFavorited(userId, id));
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            log.error("获取食谱详情失败", e);
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
        boolean favorited = recipeService.toggleFavorite(userId, id);
        return ResponseEntity.ok(ApiResponse.success(favorited ? "已收藏" : "已取消收藏",
                Map.of("favorited", favorited)));
    }

    /**
     * 评分（需登录）
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<RecipeRating>> rate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        int score = (int) body.getOrDefault("score", 5);
        String comment = (String) body.get("comment");
        RecipeRating rating = recipeService.rateRecipe(userId, id, score, comment);
        return ResponseEntity.ok(ApiResponse.success("评分成功", rating));
    }

    /**
     * 我的收藏食谱（需登录）
     */
    @GetMapping("/my-favorites")
    public ResponseEntity<ApiResponse<Page<RecipeFavorite>>> myFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "请先登录"));
        return ResponseEntity.ok(ApiResponse.success("获取成功", recipeService.getUserFavorites(userId, page, size)));
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
