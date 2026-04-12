package com.nutriai.repository;

import com.nutriai.entity.RecipeCorpus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeCorpusRepository extends JpaRepository<RecipeCorpus, Long> {

    Page<RecipeCorpus> findByCategory(String category, Pageable pageable);

    @Query(value = "SELECT * FROM recipe_corpus WHERE MATCH(name, description) AGAINST(:keyword IN BOOLEAN MODE) LIMIT :limit",
           nativeQuery = true)
    List<RecipeCorpus> fullTextSearch(@Param("keyword") String keyword, @Param("limit") int limit);

    @Query(value = "SELECT * FROM recipe_corpus WHERE name LIKE CONCAT('%', :keyword, '%') LIMIT :limit",
           nativeQuery = true)
    List<RecipeCorpus> searchByName(@Param("keyword") String keyword, @Param("limit") int limit);

    @Query(value = "SELECT * FROM recipe_corpus WHERE name LIKE CONCAT('%', :keyword, '%') OR dish LIKE CONCAT('%', :keyword, '%')",
           countQuery = "SELECT COUNT(*) FROM recipe_corpus WHERE name LIKE CONCAT('%', :keyword, '%') OR dish LIKE CONCAT('%', :keyword, '%')",
           nativeQuery = true)
    Page<RecipeCorpus> searchPaged(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM recipe_corpus WHERE category = :category",
           countQuery = "SELECT COUNT(*) FROM recipe_corpus WHERE category = :category",
           nativeQuery = true)
    Page<RecipeCorpus> findByCategoryPaged(@Param("category") String category, Pageable pageable);

    @Query(value = "SELECT DISTINCT category FROM recipe_corpus", nativeQuery = true)
    List<String> findAllCategories();

    @Query(value = "SELECT COUNT(*) FROM recipe_corpus", nativeQuery = true)
    long countAll();
}
