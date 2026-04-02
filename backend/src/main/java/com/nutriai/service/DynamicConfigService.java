package com.nutriai.service;

import com.nutriai.entity.SystemConfig;
import com.nutriai.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态配置服务 — 统一配置读取入口
 * 优先级：数据库 system_config 表 > Spring Environment（.env / application.yml）
 * 带本地缓存，通过 invalidate() 清除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicConfigService {

    private final SystemConfigRepository configRepository;
    private final Environment environment;

    /** 缓存：key -> Optional<value>（null 表示数据库中无此 key） */
    private final ConcurrentHashMap<String, Optional<String>> cache = new ConcurrentHashMap<>();

    // ─── 公共读取方法 ─────────────────────────────────────────

    /**
     * 获取字符串配置值
     * @param dbKey 数据库中的 config_key（如 "ai.model"）
     * @param envKey Spring Environment key（如 "ai.model-name"），为 null 则不走 env 回退
     * @param defaultValue 最终兜底值
     */
    public String getString(String dbKey, String envKey, String defaultValue) {
        // 1. 尝试数据库
        Optional<String> cached = cache.computeIfAbsent(dbKey, k -> {
            Optional<SystemConfig> opt = configRepository.findByConfigKey(k);
            return opt.map(SystemConfig::getConfigValue);
        });
        if (cached.isPresent() && !cached.get().isEmpty()) {
            return cached.get();
        }
        // 2. 尝试 Spring Environment
        if (envKey != null) {
            String envVal = environment.getProperty(envKey);
            if (envVal != null && !envVal.isEmpty()) {
                return envVal;
            }
        }
        // 3. 兜底
        return defaultValue;
    }

    /** 简化重载：dbKey == envKey */
    public String getString(String key, String defaultValue) {
        return getString(key, null, defaultValue);
    }

    public int getInt(String dbKey, String envKey, int defaultValue) {
        String val = getString(dbKey, envKey, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 {} 值 '{}' 无法转为 int，使用默认值 {}", dbKey, val, defaultValue);
            return defaultValue;
        }
    }

    public double getDouble(String dbKey, String envKey, double defaultValue) {
        String val = getString(dbKey, envKey, null);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 {} 值 '{}' 无法转为 double，使用默认值 {}", dbKey, val, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(String dbKey, String envKey, boolean defaultValue) {
        String val = getString(dbKey, envKey, null);
        if (val == null) return defaultValue;
        return "true".equalsIgnoreCase(val.trim()) || "1".equals(val.trim());
    }

    // ─── 缓存管理 ─────────────────────────────────────────

    /** 清除单个 key 的缓存 */
    public void invalidate(String dbKey) {
        cache.remove(dbKey);
        log.debug("配置缓存已失效: {}", dbKey);
    }

    /** 清除全部缓存（配置批量更新后调用） */
    public void invalidateAll() {
        cache.clear();
        log.info("全部配置缓存已清除");
    }
}
