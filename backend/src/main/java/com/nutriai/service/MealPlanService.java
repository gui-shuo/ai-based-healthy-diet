package com.nutriai.service;

import com.nutriai.entity.*;
import com.nutriai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanFavoriteRepository favoriteRepository;

    /**
     * 分页查询健康餐（支持筛选）
     */
    public Page<MealPlan> getMealPlans(int page, int size, String dietGoal, String planType, String keyword, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<MealPlan> result;
        if (keyword != null && !keyword.isBlank()) {
            result = mealPlanRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            result = mealPlanRepository.findWithFilters(
                    blankToNull(dietGoal), blankToNull(planType), pageable);
        }
        result.getContent().forEach(this::clearLazyCollections);
        return result;
    }

    /**
     * 获取精选健康餐
     */
    public List<MealPlan> getFeaturedMealPlans() {
        List<MealPlan> list = mealPlanRepository.findByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();
        list.forEach(this::clearLazyCollections);
        return list;
    }

    /**
     * 获取健康餐详情（含每日安排和每餐条目）
     */
    @Transactional
    public MealPlan getMealPlanDetail(Long id) {
        MealPlan plan = mealPlanRepository.findById(id).orElse(null);
        if (plan == null || !plan.getIsActive()) return null;
        mealPlanRepository.incrementViewCount(id);
        // Force lazy load
        if (plan.getDays() != null) {
            plan.getDays().forEach(day -> {
                if (day.getItems() != null) day.getItems().size();
            });
        }
        return plan;
    }

    /**
     * 收藏/取消收藏
     */
    @Transactional
    public boolean toggleFavorite(Long userId, Long mealPlanId) {
        Optional<MealPlanFavorite> existing = favoriteRepository.findByUserIdAndMealPlanId(userId, mealPlanId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            mealPlanRepository.updateFavoriteCount(mealPlanId, -1);
            return false;
        } else {
            favoriteRepository.save(MealPlanFavorite.builder().userId(userId).mealPlanId(mealPlanId).build());
            mealPlanRepository.updateFavoriteCount(mealPlanId, 1);
            return true;
        }
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long userId, Long mealPlanId) {
        return favoriteRepository.existsByUserIdAndMealPlanId(userId, mealPlanId);
    }

    /**
     * 获取用户收藏列表
     */
    public Page<MealPlanFavorite> getUserFavorites(Long userId, int page, int size) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size));
    }

    // ========== Admin Methods ==========

    public Page<MealPlan> adminListMealPlans(int page, int size) {
        Page<MealPlan> result = mealPlanRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, size));
        result.getContent().forEach(this::clearLazyCollections);
        return result;
    }

    @Transactional
    public MealPlan createMealPlan(MealPlan plan) {
        MealPlan saved = mealPlanRepository.save(plan);
        if (saved.getDays() != null) {
            saved.getDays().forEach(day -> {
                day.setMealPlanId(saved.getId());
                if (day.getItems() != null) {
                    day.getItems().forEach(item -> item.setMealPlanDayId(day.getId()));
                }
            });
        }
        return mealPlanRepository.save(saved);
    }

    @Transactional
    public MealPlan updateMealPlan(Long id, MealPlan updated) {
        MealPlan existing = mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("健康餐不存在"));
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCoverImage(updated.getCoverImage());
        existing.setPlanType(updated.getPlanType());
        existing.setDietGoal(updated.getDietGoal());
        existing.setTargetCalories(updated.getTargetCalories());
        existing.setTargetProtein(updated.getTargetProtein());
        existing.setTargetFat(updated.getTargetFat());
        existing.setTargetCarbs(updated.getTargetCarbs());
        existing.setDurationDays(updated.getDurationDays());
        existing.setSuitableCrowd(updated.getSuitableCrowd());
        existing.setIsFeatured(updated.getIsFeatured());
        existing.setIsActive(updated.getIsActive());

        if (updated.getDays() != null) {
            existing.getDays().clear();
            updated.getDays().forEach(day -> {
                day.setMealPlanId(id);
                if (day.getItems() != null) {
                    day.getItems().forEach(item -> item.setMealPlanDayId(day.getId()));
                }
                existing.getDays().add(day);
            });
        }

        return mealPlanRepository.save(existing);
    }

    @Transactional
    public void deleteMealPlan(Long id) {
        mealPlanRepository.deleteById(id);
    }

    @Transactional
    public void toggleActive(Long id) {
        MealPlan plan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("健康餐不存在"));
        plan.setIsActive(!plan.getIsActive());
        mealPlanRepository.save(plan);
    }

    @Transactional
    public void toggleFeatured(Long id) {
        MealPlan plan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("健康餐不存在"));
        plan.setIsFeatured(!plan.getIsFeatured());
        mealPlanRepository.save(plan);
    }

    // ========== Helpers ==========

    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortObj;
        if ("popular".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "viewCount");
        } else if ("calories_asc".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "targetCalories");
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return PageRequest.of(page - 1, size, sortObj);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private void clearLazyCollections(MealPlan plan) {
        plan.setDays(null);
    }
}
