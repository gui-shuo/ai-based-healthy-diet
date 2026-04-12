package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.service.AdminAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台告警测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/alerts")
@RequiredArgsConstructor
public class AdminAlertController {
    
    private final AdminAlertService adminAlertService;
    
    /**
     * 发送测试告警
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> sendTestAlert(
            @RequestBody Map<String, String> request) {
        try {
            String level = request.getOrDefault("level", "info");
            String title = request.getOrDefault("title", "测试告警");
            String content = request.getOrDefault("content", "这是一条测试告警消息");
            
            switch (level) {
                case "warning":
                    adminAlertService.sendWarningAlert(title, content);
                    break;
                case "error":
                    adminAlertService.sendErrorAlert(title, content);
                    break;
                default:
                    adminAlertService.sendInfoAlert(title, content);
            }
            
            return ResponseEntity.ok(ApiResponse.success("告警已发送"));
        } catch (Exception e) {
            log.error("发送测试告警失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取在线管理员数量
     */
    @GetMapping("/online-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> getOnlineAdminCount() {
        try {
            int count = adminAlertService.getOnlineAdminCount();
            return ResponseEntity.ok(ApiResponse.success("获取成功", count));
        } catch (Exception e) {
            log.error("获取在线管理员数量失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 模拟系统告警
     */
    @PostMapping("/simulate/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> simulateAlert(@PathVariable String type) {
        try {
            switch (type) {
                case "user-register":
                    adminAlertService.sendUserRegisterAlert("测试用户" + System.currentTimeMillis());
                    break;
                case "ai-failure":
                    adminAlertService.sendAIFailureAlert(1L, "API调用超时");
                    break;
                case "performance":
                    adminAlertService.sendPerformanceAlert("CPU使用率", 85.5, 80.0);
                    break;
                case "database":
                    adminAlertService.sendDatabaseAlert("数据库连接池已满");
                    break;
                default:
                    adminAlertService.sendInfoAlert("系统通知", "这是一条模拟告警");
            }
            
            return ResponseEntity.ok(ApiResponse.success("模拟告警已发送"));
        } catch (Exception e) {
            log.error("模拟告警失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "模拟失败: " + e.getMessage()));
        }
    }
}
