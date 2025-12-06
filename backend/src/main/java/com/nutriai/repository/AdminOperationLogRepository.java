package com.nutriai.repository;

import com.nutriai.entity.AdminOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员操作日志Repository
 */
@Repository
public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
    
    /**
     * 根据管理员ID查询日志（分页）
     */
    Page<AdminOperationLog> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
    
    /**
     * 根据操作类型查询日志（分页）
     */
    Page<AdminOperationLog> findByOperationTypeOrderByCreatedAtDesc(String operationType, Pageable pageable);
    
    /**
     * 根据时间范围查询日志（分页）
     */
    Page<AdminOperationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );
    
    /**
     * 查询最近的操作日志
     */
    List<AdminOperationLog> findTop10ByOrderByCreatedAtDesc();
}
