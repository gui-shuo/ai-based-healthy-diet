package com.nutriai.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于调试和日志记录
 */
@Slf4j
@Component
public class AdminWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("=== WebSocket握手开始 ===");
        log.info("Request URI: {}", request.getURI());
        log.info("Request Headers: {}", request.getHeaders());
        log.info("Remote Address: {}", request.getRemoteAddress());
        
        // 允许握手继续
        return true;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("❌ WebSocket握手失败", exception);
        } else {
            log.info("✅ WebSocket握手成功");
        }
    }
}
