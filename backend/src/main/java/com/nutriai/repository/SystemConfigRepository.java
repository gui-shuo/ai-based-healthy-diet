package com.nutriai.repository;

import com.nutriai.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * 根据配置键查询
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    /**
     * 根据分类查询
     */
    List<SystemConfig> findByCategory(String category);
    
    /**
     * 查询所有公开配置
     */
    List<SystemConfig> findByIsPublicTrue();
    
    /**
     * 根据分类查询公开配置
     */
    List<SystemConfig> findByCategoryAndIsPublicTrue(String category);
    
    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);
}
