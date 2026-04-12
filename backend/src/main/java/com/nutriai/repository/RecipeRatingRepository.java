package com.nutriai.repository;

import com.nutriai.entity.RecipeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface RecipeRatingRepository extends JpaRepository<RecipeRating, Long> {
    Optional<RecipeRating> findByUserIdAndRecipeId(Long userId, Long recipeId);

    @Query("SELECT AVG(r.score) FROM RecipeRating r WHERE r.recipeId = :recipeId")
    BigDecimal getAverageScore(@Param("recipeId") Long recipeId);

    @Query("SELECT COUNT(r) FROM RecipeRating r WHERE r.recipeId = :recipeId")
    int countByRecipeId(@Param("recipeId") Long recipeId);
}
