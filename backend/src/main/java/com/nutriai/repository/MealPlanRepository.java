package com.nutriai.repository;

import com.nutriai.entity.MealPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Page<MealPlan> findByIsActiveTrue(Pageable pageable);

    Page<MealPlan> findByIsActiveTrueAndDietGoal(String dietGoal, Pageable pageable);

    Page<MealPlan> findByIsActiveTrueAndPlanType(String planType, Pageable pageable);

    @Query("SELECT m FROM MealPlan m WHERE m.isActive = true " +
           "AND (:dietGoal IS NULL OR m.dietGoal = :dietGoal) " +
           "AND (:planType IS NULL OR m.planType = :planType)")
    Page<MealPlan> findWithFilters(
            @Param("dietGoal") String dietGoal,
            @Param("planType") String planType,
            Pageable pageable);

    List<MealPlan> findByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE MealPlan m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE MealPlan m SET m.favoriteCount = m.favoriteCount + :delta WHERE m.id = :id")
    void updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

    Page<MealPlan> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT m FROM MealPlan m WHERE m.isActive = true AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MealPlan> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
