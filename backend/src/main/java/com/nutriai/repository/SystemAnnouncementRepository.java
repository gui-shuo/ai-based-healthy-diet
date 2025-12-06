package com.nutriai.repository;

import com.nutriai.entity.SystemAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统公告Repository
 */
@Repository
public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, Long> {
    
    /**
     * 查询所有启用的公告（按优先级排序）
     */
    List<SystemAnnouncement> findByIsActiveTrueOrderByPriorityDescCreatedAtDesc();
    
    /**
     * 查询当前有效的公告
     */
    @Query("SELECT a FROM SystemAnnouncement a WHERE a.isActive = true AND " +
           "(a.startTime IS NULL OR a.startTime <= :now) AND " +
           "(a.endTime IS NULL OR a.endTime >= :now) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<SystemAnnouncement> findActiveAnnouncements(@Param("now") LocalDateTime now);
    
    /**
     * 根据类型查询公告
     */
    List<SystemAnnouncement> findByTypeOrderByPriorityDescCreatedAtDesc(String type);
    
    /**
     * 查询所有公告（按优先级和创建时间排序）
     */
    List<SystemAnnouncement> findAllByOrderByPriorityDescCreatedAtDesc();
    
    /**
     * 分页查询启用的公告（用户端）
     */
    Page<SystemAnnouncement> findByIsActiveTrueOrderByPriorityDescCreatedAtDesc(Pageable pageable);
}
