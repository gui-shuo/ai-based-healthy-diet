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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanFavoriteRepository favoriteRepository;
    private final MealPlanFollowRepository followRepository;
    private final MealPlanCheckinRepository checkinRepository;
    private final MealPlanRatingRepository ratingRepository;

    // ========== Browse & Discovery ==========

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

    public List<MealPlan> getFeaturedMealPlans() {
        List<MealPlan> list = mealPlanRepository.findByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();
        list.forEach(this::clearLazyCollections);
        return list;
    }

    @Transactional
    public MealPlan getMealPlanDetail(Long id) {
        MealPlan plan = mealPlanRepository.findById(id).orElse(null);
        if (plan == null || !plan.getIsActive()) return null;
        mealPlanRepository.incrementViewCount(id);
        if (plan.getDays() != null) {
            plan.getDays().forEach(day -> {
                if (day.getItems() != null) day.getItems().size();
            });
        }
        return plan;
    }

    public List<String> getAllTags() {
        List<MealPlan> plans = mealPlanRepository.findByIsActiveTrue();
        Set<String> tagSet = new TreeSet<>();
        for (MealPlan p : plans) {
            if (p.getTags() != null && !p.getTags().isBlank()) {
                for (String tag : p.getTags().split(",")) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) tagSet.add(tag);
                }
            }
        }
        return new ArrayList<>(tagSet);
    }

    public Page<MealPlan> searchByTag(String tag, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MealPlan> result = mealPlanRepository.searchByTag(tag, pageable);
        result.getContent().forEach(this::clearLazyCollections);
        return result;
    }

    public List<MealPlan> getRecommendations(Long userId) {
        List<MealPlan> featured = mealPlanRepository.findByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();
        featured.forEach(this::clearLazyCollections);
        return featured.size() > 6 ? featured.subList(0, 6) : featured;
    }

    // ========== Favorites ==========

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

    public boolean isFavorited(Long userId, Long mealPlanId) {
        return favoriteRepository.existsByUserIdAndMealPlanId(userId, mealPlanId);
    }

    public Page<MealPlanFavorite> getUserFavorites(Long userId, int page, int size) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(Math.max(page - 1, 0), size));
    }

    // ========== Follow & Track ==========

    @Transactional
    public MealPlanFollow followPlan(Long userId, Long mealPlanId) {
        Optional<MealPlanFollow> existing = followRepository.findByUserIdAndMealPlanId(userId, mealPlanId);
        if (existing.isPresent()) {
            MealPlanFollow follow = existing.get();
            if ("COMPLETED".equals(follow.getStatus()) || "ABANDONED".equals(follow.getStatus())) {
                follow.setStatus("ACTIVE");
                follow.setCurrentDay(1);
                follow.setStartDate(LocalDate.now());
                followRepository.save(follow);
                mealPlanRepository.updateFollowCount(mealPlanId, 1);
            }
            return follow;
        }
        MealPlanFollow follow = MealPlanFollow.builder()
                .userId(userId)
                .mealPlanId(mealPlanId)
                .startDate(LocalDate.now())
                .currentDay(1)
                .status("ACTIVE")
                .build();
        followRepository.save(follow);
        mealPlanRepository.updateFollowCount(mealPlanId, 1);
        return follow;
    }

    @Transactional
    public void unfollowPlan(Long userId, Long mealPlanId) {
        Optional<MealPlanFollow> follow = followRepository.findByUserIdAndMealPlanId(userId, mealPlanId);
        if (follow.isPresent()) {
            follow.get().setStatus("ABANDONED");
            followRepository.save(follow.get());
            mealPlanRepository.updateFollowCount(mealPlanId, -1);
        }
    }

    public MealPlanFollow getFollowStatus(Long userId, Long mealPlanId) {
        return followRepository.findByUserIdAndMealPlanId(userId, mealPlanId).orElse(null);
    }

    public List<MealPlanFollow> getActiveFollows(Long userId) {
        List<MealPlanFollow> follows = followRepository.findActiveByUserId(userId);
        for (MealPlanFollow f : follows) {
            mealPlanRepository.findById(f.getMealPlanId()).ifPresent(plan -> {
                clearLazyCollections(plan);
                f.setMealPlan(plan);
            });
        }
        return follows;
    }

    public Map<String, Object> getFollowProgress(Long userId, Long mealPlanId) {
        MealPlanFollow follow = followRepository.findByUserIdAndMealPlanId(userId, mealPlanId).orElse(null);
        if (follow == null) return null;

        MealPlan plan = mealPlanRepository.findById(mealPlanId).orElse(null);
        if (plan == null) return null;

        int totalDays = plan.getDurationDays();
        int checkedDays = checkinRepository.countCheckedDays(follow.getId());
        int totalCheckins = checkinRepository.countCheckins(follow.getId());
        List<MealPlanCheckin> checkins = checkinRepository.findByFollowId(follow.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("follow", follow);
        result.put("totalDays", totalDays);
        result.put("checkedDays", checkedDays);
        result.put("totalCheckins", totalCheckins);
        result.put("progress", totalDays > 0 ? Math.round((double) checkedDays / totalDays * 100) : 0);
        result.put("checkins", checkins);
        return result;
    }

    // ========== Check-in ==========

    @Transactional
    public MealPlanCheckin checkin(Long userId, Long followId, int dayNumber, String mealType) {
        MealPlanFollow follow = followRepository.findById(followId).orElse(null);
        if (follow == null || !follow.getUserId().equals(userId)) {
            throw new RuntimeException("跟随记录不存在");
        }

        Optional<MealPlanCheckin> existing = checkinRepository.findByFollowIdAndDayNumberAndMealType(followId, dayNumber, mealType);
        if (existing.isPresent()) {
            return existing.get();
        }

        MealPlanCheckin checkin = MealPlanCheckin.builder()
                .followId(followId)
                .userId(userId)
                .dayNumber(dayNumber)
                .mealType(mealType)
                .build();
        checkinRepository.save(checkin);

        if (dayNumber > follow.getCurrentDay()) {
            followRepository.updateCurrentDay(followId, dayNumber);
        }

        MealPlan plan = mealPlanRepository.findById(follow.getMealPlanId()).orElse(null);
        if (plan != null && dayNumber >= plan.getDurationDays()) {
            int checkedDays = checkinRepository.countCheckedDays(followId);
            if (checkedDays >= plan.getDurationDays()) {
                follow.setStatus("COMPLETED");
                followRepository.save(follow);
            }
        }

        return checkin;
    }

    @Transactional
    public void uncheckin(Long userId, Long followId, int dayNumber, String mealType) {
        Optional<MealPlanCheckin> existing = checkinRepository.findByFollowIdAndDayNumberAndMealType(followId, dayNumber, mealType);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            checkinRepository.delete(existing.get());
        }
    }

    public List<MealPlanCheckin> getDayCheckins(Long followId, int dayNumber) {
        return checkinRepository.findByFollowIdAndDayNumber(followId, dayNumber);
    }

    // ========== Ratings ==========

    @Transactional
    public MealPlanRating ratePlan(Long userId, Long mealPlanId, int rating, String review) {
        if (rating < 1 || rating > 5) throw new RuntimeException("评分范围1-5");

        Optional<MealPlanRating> existing = ratingRepository.findByUserIdAndMealPlanId(userId, mealPlanId);
        MealPlanRating r;
        if (existing.isPresent()) {
            r = existing.get();
            r.setRating(rating);
            r.setReview(review);
        } else {
            r = MealPlanRating.builder()
                    .userId(userId)
                    .mealPlanId(mealPlanId)
                    .rating(rating)
                    .review(review)
                    .build();
        }
        ratingRepository.save(r);

        Double avg = ratingRepository.getAverageRating(mealPlanId);
        int count = ratingRepository.countByMealPlanId(mealPlanId);
        mealPlanRepository.updateRating(mealPlanId,
                avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                count);

        return r;
    }

    public Page<MealPlanRating> getPlanRatings(Long mealPlanId, int page, int size) {
        return ratingRepository.findByMealPlanIdOrderByCreatedAtDesc(mealPlanId, PageRequest.of(Math.max(page - 1, 0), size));
    }

    public MealPlanRating getUserRating(Long userId, Long mealPlanId) {
        return ratingRepository.findByUserIdAndMealPlanId(userId, mealPlanId).orElse(null);
    }

    // ========== Admin Methods ==========

    public Page<MealPlan> adminListMealPlans(int page, int size) {
        Page<MealPlan> result = mealPlanRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page - 1, 0), size));
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
                .orElseThrow(() -> new RuntimeException("营养餐不存在"));
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
        existing.setTags(updated.getTags());
        existing.setDifficulty(updated.getDifficulty());
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
                .orElseThrow(() -> new RuntimeException("营养餐不存在"));
        plan.setIsActive(!plan.getIsActive());
        mealPlanRepository.save(plan);
    }

    @Transactional
    public void toggleFeatured(Long id) {
        MealPlan plan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("营养餐不存在"));
        plan.setIsFeatured(!plan.getIsFeatured());
        mealPlanRepository.save(plan);
    }

    // ========== Helpers ==========

    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortObj;
        switch (sort != null ? sort : "latest") {
            case "popular":
                sortObj = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            case "rating":
                sortObj = Sort.by(Sort.Direction.DESC, "avgRating");
                break;
            case "followers":
                sortObj = Sort.by(Sort.Direction.DESC, "followCount");
                break;
            case "calories_asc":
                sortObj = Sort.by(Sort.Direction.ASC, "targetCalories");
                break;
            default:
                sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return PageRequest.of(Math.max(page - 1, 0), size, sortObj);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private void clearLazyCollections(MealPlan plan) {
        plan.setDays(null);
    }
}
