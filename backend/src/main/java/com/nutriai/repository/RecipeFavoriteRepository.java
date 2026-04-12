package com.nutriai.repository;

import com.nutriai.entity.RecipeFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecipeFavoriteRepository extends JpaRepository<RecipeFavorite, Long> {
    Optional<RecipeFavorite> findByUserIdAndRecipeId(Long userId, Long recipeId);
    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);
    Page<RecipeFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
}
