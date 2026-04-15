package com.nutriai.repository;

import com.nutriai.entity.MealItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    Page<MealItem> findByIsAvailableTrueOrderBySortOrderAsc(Pageable pageable);

    Page<MealItem> findByCategoryAndIsAvailableTrueOrderBySortOrderAsc(String category, Pageable pageable);

    Page<MealItem> findByMealTypeAndIsAvailableTrueOrderBySortOrderAsc(String mealType, Pageable pageable);

    List<MealItem> findByIsRecommendedTrueAndIsAvailableTrueOrderBySortOrderAsc();

    @Query("SELECT m FROM MealItem m WHERE m.isAvailable = true AND (LOWER(m.name) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(m.brief) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<MealItem> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT m.category FROM MealItem m WHERE m.isAvailable = true")
    List<String> findDistinctCategories();

    // Admin queries (include unavailable)
    Page<MealItem> findAllByOrderBySortOrderAsc(Pageable pageable);

    @Query("SELECT m FROM MealItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(m.brief) LIKE LOWER(CONCAT('%',:keyword,'%'))")
    Page<MealItem> adminSearchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
