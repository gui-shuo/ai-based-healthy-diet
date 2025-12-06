package com.nutriai.repository;

import com.nutriai.entity.AIChatFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI聊天收藏Repository
 */
@Repository
public interface AIChatFavoriteRepository extends JpaRepository<AIChatFavorite, Long> {
    
    /**
     * 根据用户ID查询收藏列表（分页）
     */
    Page<AIChatFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID查询所有收藏
     */
    List<AIChatFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据ID和用户ID查询
     */
    Optional<AIChatFavorite> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 删除用户的所有收藏
     */
    void deleteByUserId(Long userId);
}
