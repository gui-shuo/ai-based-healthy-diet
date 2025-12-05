package com.nutriai.dto;

import lombok.Data;

/**
 * 任务状态响应
 */
@Data
public class TaskStatusResponse {
    
    private String taskId;
    private String status; // pending, running, completed, failed, cancelled
    private Integer progress;
    private Integer totalDays;
    private Integer currentDay;
    private String planId; // 完成后才有
    private String errorMessage;
    
    public static TaskStatusResponse fromEntity(com.nutriai.entity.DietPlanTask task) {
        TaskStatusResponse response = new TaskStatusResponse();
        response.setTaskId(task.getTaskId());
        response.setStatus(task.getStatus());
        response.setProgress(task.getProgress());
        response.setTotalDays(task.getTotalDays());
        response.setCurrentDay(task.getCurrentDay());
        response.setPlanId(task.getPlanId());
        response.setErrorMessage(task.getErrorMessage());
        return response;
    }
}
