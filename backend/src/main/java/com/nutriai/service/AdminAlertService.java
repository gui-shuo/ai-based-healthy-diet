package com.nutriai.service;

import com.nutriai.websocket.AdminAlertWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 管理后台告警服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAlertService {
    
    private final AdminAlertWebSocketHandler adminAlertWebSocketHandler;
    
    /**
     * 发送信息级别告警
     */
    @Async
    public void sendInfoAlert(String title, String content) {
        log.info("发送信息告警: {}", title);
        adminAlertWebSocketHandler.broadcastAlert("info", title, content);
    }
    
    /**
     * 发送警告级别告警
     */
    @Async
    public void sendWarningAlert(String title, String content) {
        log.warn("发送警告告警: {}", title);
        adminAlertWebSocketHandler.broadcastAlert("warning", title, content);
    }
    
    /**
     * 发送错误级别告警
     */
    @Async
    public void sendErrorAlert(String title, String content) {
        log.error("发送错误告警: {}", title);
        adminAlertWebSocketHandler.broadcastAlert("error", title, content);
    }
    
    /**
     * 发送告警给特定管理员
     */
    @Async
    public void sendAlertToAdmin(Long userId, String level, String title, String content) {
        log.info("发送告警给管理员: userId={}, level={}, title={}", userId, level, title);
        adminAlertWebSocketHandler.sendAlertToAdmin(userId, level, title, content);
    }
    
    /**
     * 系统启动告警
     */
    public void sendSystemStartAlert() {
        sendInfoAlert("系统启动", "AI健康饮食系统已成功启动");
    }
    
    /**
     * 用户注册告警
     */
    public void sendUserRegisterAlert(String username) {
        sendInfoAlert("新用户注册", String.format("用户 %s 已成功注册", username));
    }
    
    /**
     * AI调用失败告警
     */
    public void sendAIFailureAlert(Long userId, String error) {
        sendErrorAlert("AI调用失败", 
                String.format("用户ID: %d, 错误信息: %s", userId, error));
    }
    
    /**
     * 系统性能告警
     */
    public void sendPerformanceAlert(String metric, double value, double threshold) {
        sendWarningAlert("系统性能告警", 
                String.format("%s 当前值: %.2f, 阈值: %.2f", metric, value, threshold));
    }
    
    /**
     * 数据库连接告警
     */
    public void sendDatabaseAlert(String message) {
        sendErrorAlert("数据库告警", message);
    }
    
    /**
     * 获取在线管理员数量
     */
    public int getOnlineAdminCount() {
        return adminAlertWebSocketHandler.getOnlineAdminCount();
    }
}
