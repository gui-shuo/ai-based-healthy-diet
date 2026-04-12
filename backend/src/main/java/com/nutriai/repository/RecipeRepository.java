package com.nutriai.repository;

import com.nutriai.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByIsActiveTrue(Pageable pageable);

    Page<Recipe> findByIsActiveTrueAndCategory(String category, Pageable pageable);

    Page<Recipe> findByIsActiveTrueAndDietType(String dietType, Pageable pageable);

    Page<Recipe> findByIsActiveTrueAndCuisineType(String cuisineType, Pageable pageable);

    Page<Recipe> findByIsActiveTrueAndDifficulty(String difficulty, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.isActive = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Recipe> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.isActive = true " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:dietType IS NULL OR r.dietType = :dietType) " +
           "AND (:cuisineType IS NULL OR r.cuisineType = :cuisineType) " +
           "AND (:difficulty IS NULL OR r.difficulty = :difficulty)")
    Page<Recipe> findWithFilters(
            @Param("category") String category,
            @Param("dietType") String dietType,
            @Param("cuisineType") String cuisineType,
            @Param("difficulty") String difficulty,
            Pageable pageable);

    List<Recipe> findByIsActiveTrueAndIsFeaturedTrueOrderByRatingAvgDesc();

    @Modifying
    @Query("UPDATE Recipe r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Recipe r SET r.favoriteCount = r.favoriteCount + :delta WHERE r.id = :id")
    void updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Recipe r SET r.ratingAvg = :avg, r.ratingCount = :count WHERE r.id = :id")
    void updateRating(@Param("id") Long id, @Param("avg") java.math.BigDecimal avg, @Param("count") int count);

    Page<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.isActive = true AND r.id IN " +
           "(SELECT rt.recipeId FROM RecipeTag rt WHERE rt.tag = :tag)")
    Page<Recipe> findByTag(@Param("tag") String tag, Pageable pageable);
}
