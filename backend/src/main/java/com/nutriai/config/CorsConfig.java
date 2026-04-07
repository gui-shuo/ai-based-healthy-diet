package com.nutriai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * 提供 CorsConfigurationSource Bean，供 Spring Security 的 .cors(withDefaults()) 使用
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // WebSocket端点：允许所有来源（APP原生WebSocket发送Origin:null）
        CorsConfiguration wsConfig = new CorsConfiguration();
        wsConfig.setAllowedOriginPatterns(List.of("*"));
        wsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        wsConfig.setAllowedHeaders(List.of("*"));
        wsConfig.setAllowCredentials(true);
        source.registerCorsConfiguration("/ws/**", wsConfig);
        
        // 其他API端点：使用配置的允许来源
        CorsConfiguration config = new CorsConfiguration();
        if ("*".equals(allowedOrigins)) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        }
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Request-ID"));
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
}
