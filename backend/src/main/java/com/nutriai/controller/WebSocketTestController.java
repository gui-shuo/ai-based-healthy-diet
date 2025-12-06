package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.websocket.AdminAlertWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class WebSocketTestController {
    
    private final AdminAlertWebSocketHandler adminAlertWebSocketHandler;
    
    /**
     * 测试WebSocket状态
     */
    @GetMapping("/websocket-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWebSocketStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("handlerExists", adminAlertWebSocketHandler != null);
        status.put("onlineAdmins", adminAlertWebSocketHandler.getOnlineAdminCount());
        status.put("endpoint", "ws://localhost:8080/ws/admin/alerts");
        
        log.info("WebSocket状态检查: {}", status);
        return ResponseEntity.ok(ApiResponse.success("WebSocket状态", status));
    }
}
