package com.nutriai.repository;

import com.nutriai.entity.MealPlanFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanFollowRepository extends JpaRepository<MealPlanFollow, Long> {

    Optional<MealPlanFollow> findByUserIdAndMealPlanId(Long userId, Long mealPlanId);

    boolean existsByUserIdAndMealPlanId(Long userId, Long mealPlanId);

    List<MealPlanFollow> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    Page<MealPlanFollow> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT f FROM MealPlanFollow f WHERE f.userId = :userId AND f.status = 'ACTIVE'")
    List<MealPlanFollow> findActiveByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE MealPlanFollow f SET f.currentDay = :day WHERE f.id = :id")
    void updateCurrentDay(@Param("id") Long id, @Param("day") int day);
}
