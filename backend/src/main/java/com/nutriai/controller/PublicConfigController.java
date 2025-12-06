package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.service.AdminConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开配置控制器 - 用户前端可访问
 */
@Slf4j
@RestController
@RequestMapping("/public/config")
@RequiredArgsConstructor
public class PublicConfigController {
    
    private final AdminConfigService configService;
    
    /**
     * 获取公开配置
     * 用户前端可以访问的配置项
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicConfig() {
        try {
            Map<String, String> publicConfig = new HashMap<>();
            
            // 获取所有公开配置
            var configs = configService.getAllConfigs();
            
            for (var config : configs) {
                if (config.getIsPublic() != null && config.getIsPublic()) {
                    publicConfig.put(config.getConfigKey(), config.getConfigValue());
                }
            }
            
            log.info("获取公开配置: {} 个", publicConfig.size());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", publicConfig));
        } catch (Exception e) {
            log.error("获取公开配置失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取特定配置值
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> getConfigValue(@org.springframework.web.bind.annotation.PathVariable String key) {
        try {
            String value = configService.getPublicConfigValue(key);
            
            if (value != null) {
                return ResponseEntity.ok(ApiResponse.success("获取成功", value));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "配置不存在或未公开"));
            }
        } catch (Exception e) {
            log.error("获取配置值失败: key={}", key, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
}
