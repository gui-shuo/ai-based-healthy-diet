package com.nutriai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "nutriai-backend");
        result.put("version", "1.0.0");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "AI健康饮食规划助手后端服务运行正常");
        return result;
    }
}
