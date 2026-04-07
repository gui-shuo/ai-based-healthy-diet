package com.nutriai.config;

import com.nutriai.websocket.AIWebSocketHandler;
import com.nutriai.websocket.AdminAlertWebSocketHandler;
import com.nutriai.websocket.AdminWebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket配置类
 * 配置WebSocket端点和拦截器
 * 
 * @author NutriAI Team
 * @date 2025-12-03
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final AIWebSocketHandler aiWebSocketHandler;
    private final AdminAlertWebSocketHandler adminAlertWebSocketHandler;
    private final AdminWebSocketHandshakeInterceptor handshakeInterceptor;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("注册WebSocket处理器...");
        
        // 注册AI聊天WebSocket端点 - 原生WebSocket
        // 注意：WebSocket路径会自动加上context-path，所以实际访问路径是 /api/ws/ai/chat
        registry.addHandler(aiWebSocketHandler, "/ws/ai/chat")
                .setAllowedOrigins("*");
        
        // 注册SockJS降级支持（可选）
        registry.addHandler(aiWebSocketHandler, "/ws/ai/chat-sockjs")
                .setAllowedOrigins("*")
                .withSockJS();
        
        // 注册管理后台告警WebSocket端点（添加握手拦截器）
        // 实际访问路径：ws://localhost:8080/api/ws/admin/alerts
        registry.addHandler(adminAlertWebSocketHandler, "/ws/admin/alerts")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
        
        log.info("✅ WebSocket处理器注册完成:");
        log.info("   - /api/ws/ai/chat (原生WebSocket)");
        log.info("   - /api/ws/ai/chat-sockjs (SockJS降级支持)");
        log.info("   - /api/ws/admin/alerts (管理后台告警 + 握手拦截器)");
        log.info("⚠️  注意：WebSocket端点包含context-path前缀 /api");
    }
    
    /**
     * 配置WebSocket容器
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(65536);    // 64KB for AI streaming
        container.setMaxBinaryMessageBufferSize(16384);  // 16KB
        container.setMaxSessionIdleTimeout(300000L);     // 5 minutes
        return container;
    }
}
