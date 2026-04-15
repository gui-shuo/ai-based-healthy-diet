package com.nutriai.config;

import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import com.nutriai.service.DynamicConfigService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI配置类 - 基于 OpenAI 兼容协议，支持任意大模型供应商
 * 通过环境变量 AI_BASE_URL / AI_API_KEY / AI_MODEL_NAME 切换供应商：
 *   - 阿里云 DashScope: https://dashscope.aliyuncs.com/compatible-mode/v1
 *   - 火山引擎(豆包):    https://ark.cn-beijing.volces.com/api/v3
 *   - DeepSeek:         https://api.deepseek.com/v1
 *   - Moonshot (Kimi):  https://api.moonshot.cn/v1
 *   - 硅基流动:          https://api.siliconflow.cn/v1
 *   - 其他 OpenAI 兼容服务
 */
@Slf4j
@Configuration
public class AIConfig {

    @Lazy
    @Autowired
    private DynamicConfigService dynamicConfig;

    @Value("${ai.api-key:}")
    @Getter
    private String apiKey;

    @Value("${ai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    @Getter
    private String apiUrl;

    @Value("${ai.model-name:qwen3.5-122b-a10b}")
    @Getter
    private String modelName;

    @Value("${ai.diet-plan-model:qwen3.5-flash}")
    @Getter
    private String dietPlanModel;

    @Value("${ai.max-tokens:2000}")
    @Getter
    private Integer maxTokens;

    @Value("${ai.temperature:0.7}")
    @Getter
    private Double temperature;

    @Value("${ai.top-p:0.9}")
    @Getter
    private Double topP;

    @Value("${ai.timeout:180}")
    @Getter
    private Integer timeout;

    // ─── 动态配置读取（DB 优先 > @Value 兜底）───

    /** 获取当前生效的模型名 */
    public String getEffectiveModelName() {
        return dynamicConfig.getString("ai.model", "ai.model-name", modelName);
    }

    /** 获取当前生效的饮食计划模型名 */
    public String getEffectiveDietPlanModel() {
        return dynamicConfig.getString("ai.diet_plan_model", "ai.diet-plan-model", dietPlanModel);
    }

    /** 获取当前生效的最大Token */
    public int getEffectiveMaxTokens() {
        return dynamicConfig.getInt("ai.max_tokens", "ai.max-tokens", maxTokens);
    }

    /** 获取当前生效的温度 */
    public double getEffectiveTemperature() {
        return dynamicConfig.getDouble("ai.temperature", null, temperature);
    }

    /** 获取当前生效的 top-p */
    public double getEffectiveTopP() {
        return dynamicConfig.getDouble("ai.top_p", null, topP);
    }

    /** 获取当前生效的超时 */
    public int getEffectiveTimeout() {
        return dynamicConfig.getInt("ai.timeout", null, timeout);
    }

    private final ConcurrentHashMap<String, ChatLanguageModel> chatModelCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StreamingChatLanguageModel> streamingModelCache = new ConcurrentHashMap<>();

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化 ChatLanguageModel (OpenAI兼容) - 模型: {}, API URL: {}, Timeout: {}秒", modelName, apiUrl, timeout);
        return buildChatModel(modelName, temperature, maxTokens);
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("初始化 StreamingChatLanguageModel (OpenAI兼容) - 模型: {}, API URL: {}", modelName, apiUrl);
        return buildStreamingChatModel(modelName, temperature, maxTokens);
    }

    public ChatLanguageModel getChatModel(String model, Double temp, Integer tokens) {
        String actualModel = model != null ? model : getEffectiveModelName();
        Double actualTemp = temp != null ? temp : getEffectiveTemperature();
        Integer actualTokens = tokens != null ? tokens : getEffectiveMaxTokens();
        String cacheKey = actualModel + ":" + actualTemp + ":" + actualTokens;
        return chatModelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建ChatLanguageModel: model={}, temp={}, tokens={}", actualModel, actualTemp, actualTokens);
            return buildChatModel(actualModel, actualTemp, actualTokens);
        });
    }

    public StreamingChatLanguageModel getStreamingChatModel(String model, Double temp, Integer tokens) {
        String actualModel = model != null ? model : getEffectiveModelName();
        Double actualTemp = temp != null ? temp : getEffectiveTemperature();
        Integer actualTokens = tokens != null ? tokens : getEffectiveMaxTokens();
        String cacheKey = actualModel + ":" + actualTemp + ":" + actualTokens;
        return streamingModelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建StreamingChatLanguageModel: model={}, temp={}, tokens={}", actualModel, actualTemp, actualTokens);
            return buildStreamingChatModel(actualModel, actualTemp, actualTokens);
        });
    }

    /** 清除模型缓存（配置更改后调用） */
    public void clearModelCache() {
        chatModelCache.clear();
        streamingModelCache.clear();
        log.info("AI模型缓存已清除");
    }

    private ChatLanguageModel buildChatModel(String model, Double temp, Integer tokens) {
        return OpenAiChatModel.builder()
                .baseUrl(getEffectiveUrl())
                .apiKey(getEffectiveKey())
                .modelName(model)
                .temperature(temp)
                .topP(getEffectiveTopP())
                .maxTokens(tokens)
                .timeout(Duration.ofSeconds(getEffectiveTimeout()))
                .tokenizer(new SimpleEstimateTokenizer())
                .build();
    }

    private StreamingChatLanguageModel buildStreamingChatModel(String model, Double temp, Integer tokens) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(getEffectiveUrl())
                .apiKey(getEffectiveKey())
                .modelName(model)
                .temperature(temp)
                .topP(getEffectiveTopP())
                .maxTokens(tokens)
                .timeout(Duration.ofSeconds(getEffectiveTimeout()))
                .tokenizer(new SimpleEstimateTokenizer())
                .build();
    }

    private String getEffectiveKey() {
        String key = dynamicConfig != null
                ? dynamicConfig.getString("ai.api_key", "ai.api-key", apiKey)
                : apiKey;
        if (key == null || key.isEmpty()) {
            log.warn("⚠️ AI API Key未配置！请在管理后台系统配置或环境变量中设置");
            return "not-configured";
        }
        return key;
    }

    private String getEffectiveUrl() {
        String url = dynamicConfig != null
                ? dynamicConfig.getString("ai.base_url", "ai.base-url", apiUrl)
                : apiUrl;
        if (url != null && !url.isEmpty()) {
            return url;
        }
        return "https://dashscope.aliyuncs.com/compatible-mode/v1";
    }

    /**
     * 简单token估算器，避免jtokkit不识别非OpenAI模型名称
     * 按字符数粗略估算（中文约1.5 token/字，英文约0.25 token/字）
     */
    private static class SimpleEstimateTokenizer implements Tokenizer {
        @Override
        public int estimateTokenCountInText(String text) {
            if (text == null || text.isEmpty()) return 0;
            return (int) (text.length() * 0.6);
        }

        @Override
        public int estimateTokenCountInMessage(dev.langchain4j.data.message.ChatMessage message) {
            return estimateTokenCountInText(message.text()) + 4;
        }

        @Override
        public int estimateTokenCountInMessages(Iterable<dev.langchain4j.data.message.ChatMessage> messages) {
            int total = 3;
            for (dev.langchain4j.data.message.ChatMessage msg : messages) {
                total += estimateTokenCountInMessage(msg);
            }
            return total;
        }

        @Override
        public int estimateTokenCountInToolExecutionRequests(Iterable<dev.langchain4j.agent.tool.ToolExecutionRequest> requests) {
            return 0;
        }

        @Override
        public int estimateTokenCountInToolSpecifications(Iterable<dev.langchain4j.agent.tool.ToolSpecification> specs) {
            return 0;
        }
    }
}
