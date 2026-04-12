package com.nutriai.repository;

import com.nutriai.entity.MealPlanRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealPlanRatingRepository extends JpaRepository<MealPlanRating, Long> {

    Optional<MealPlanRating> findByUserIdAndMealPlanId(Long userId, Long mealPlanId);

    Page<MealPlanRating> findByMealPlanIdOrderByCreatedAtDesc(Long mealPlanId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM MealPlanRating r WHERE r.mealPlanId = :mealPlanId")
    Double getAverageRating(@Param("mealPlanId") Long mealPlanId);

    @Query("SELECT COUNT(r) FROM MealPlanRating r WHERE r.mealPlanId = :mealPlanId")
    int countByMealPlanId(@Param("mealPlanId") Long mealPlanId);
}
