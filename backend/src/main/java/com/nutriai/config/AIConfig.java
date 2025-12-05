package com.nutriai.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * AI配置类 - 配置LangChain4j和通义千问
 * 
 * @author NutriAI Team
 * @date 2025-12-03
 */
@Slf4j
@Configuration
public class AIConfig {
    
    @Value("${tongyi.api-key}")
    private String apiKey;
    
    @Value("${tongyi.model:qwen-max}")
    private String modelName;
    
    @Value("${tongyi.max-tokens:2000}")
    private Integer maxTokens;
    
    @Value("${tongyi.temperature:0.7}")
    private Double temperature;
    
    @Value("${tongyi.top-p:0.9}")
    private Double topP;
    
    @Value("${tongyi.timeout:180}")
    private Integer timeout;
    
    /**
     * 配置同步聊天模型
     * 用于需要等待完整响应的场景
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化ChatLanguageModel - 模型: {}, Timeout配置: {}秒", modelName, timeout);
        
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("通义千问API Key未配置！请在application.yml中设置 tongyi.api-key");
            log.warn("或设置环境变量: TONGYI_API_KEY");
        }
        
        // 通过环境变量设置DashScope的超时（DashScope SDK会读取这些环境变量）
        System.setProperty("dashscope.api.connect.timeout", String.valueOf(timeout * 1000));
        System.setProperty("dashscope.api.read.timeout", String.valueOf(timeout * 1000));
        System.setProperty("dashscope.api.write.timeout", String.valueOf(timeout * 1000));
        
        log.info("✓ 已设置DashScope超时环境变量: {}秒", timeout);
        
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
    
    /**
     * 配置流式聊天模型
     * 用于实时流式输出场景（WebSocket）
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("初始化StreamingChatLanguageModel - 模型: {}, MaxTokens: {}", modelName, maxTokens);
        
        return QwenStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
}
