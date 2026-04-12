package com.nutriai.repository;

import com.nutriai.entity.RecipeTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeTagRepository extends JpaRepository<RecipeTag, Long> {
    List<RecipeTag> findByRecipeId(Long recipeId);
    void deleteByRecipeId(Long recipeId);

    @Query("SELECT DISTINCT rt.tag FROM RecipeTag rt ORDER BY rt.tag")
    List<String> findAllDistinctTags();
}
