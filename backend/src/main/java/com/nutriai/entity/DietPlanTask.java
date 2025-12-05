package com.nutriai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 饮食计划生成任务
 */
@Data
@Entity
@Table(name = "diet_plan_tasks")
public class DietPlanTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false, unique = true, length = 50)
    private String taskId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // pending, running, completed, failed, cancelled
    
    @Column(name = "progress")
    private Integer progress;
    
    @Column(name = "total_days", nullable = false)
    private Integer totalDays;
    
    @Column(name = "current_day")
    private Integer currentDay;
    
    @Column(name = "plan_id", length = 50)
    private String planId;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
