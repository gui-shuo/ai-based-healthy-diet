package com.nutriai.repository;

import com.nutriai.entity.MealPlanFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MealPlanFavoriteRepository extends JpaRepository<MealPlanFavorite, Long> {
    Optional<MealPlanFavorite> findByUserIdAndMealPlanId(Long userId, Long mealPlanId);
    boolean existsByUserIdAndMealPlanId(Long userId, Long mealPlanId);
    Page<MealPlanFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    void deleteByUserIdAndMealPlanId(Long userId, Long mealPlanId);
}
