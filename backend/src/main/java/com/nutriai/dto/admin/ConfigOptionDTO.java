package com.nutriai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 配置选项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigOptionDTO {
    
    /**
     * 配置键
     */
    private String key;
    
    /**
     * 配置名称
     */
    private String name;
    
    /**
     * 配置说明
     */
    private String description;
    
    /**
     * 配置分类
     */
    private String category;
    
    /**
     * 值类型：string, number, boolean, select
     */
    private String valueType;
    
    /**
     * 可选值列表（当valueType为select时）
     */
    private List<OptionValue> options;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 选项值
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionValue {
        private String label;
        private String value;
    }
}
