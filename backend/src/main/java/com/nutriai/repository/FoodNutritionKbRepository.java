package com.nutriai.repository;

import com.nutriai.entity.FoodNutritionKb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodNutritionKbRepository extends JpaRepository<FoodNutritionKb, Long> {

    @Query(value = "SELECT * FROM food_nutrition_kb WHERE MATCH(food_name, alias) AGAINST(:keyword IN BOOLEAN MODE) LIMIT :limit",
           nativeQuery = true)
    List<FoodNutritionKb> fullTextSearch(@Param("keyword") String keyword, @Param("limit") int limit);

    @Query(value = "SELECT * FROM food_nutrition_kb WHERE food_name LIKE CONCAT('%', :keyword, '%') OR alias LIKE CONCAT('%', :keyword, '%') LIMIT :limit",
           nativeQuery = true)
    List<FoodNutritionKb> searchByName(@Param("keyword") String keyword, @Param("limit") int limit);
}
