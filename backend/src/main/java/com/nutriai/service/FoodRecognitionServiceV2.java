package com.nutriai.service;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriai.dto.FoodItem;
import com.nutriai.dto.FoodRecognitionResult;
import com.nutriai.entity.FoodNutrition;
import com.nutriai.entity.FoodRecognitionHistory;
import com.nutriai.repository.FoodNutritionRepository;
import com.nutriai.repository.FoodRecognitionHistoryRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 食物识别服务 V2 - 使用百度AI
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecognitionServiceV2 {
    
    private final FoodNutritionRepository foodNutritionRepository;
    private final FoodRecognitionHistoryRepository recognitionHistoryRepository;
    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private AipImageClassify aipImageClassify;
    
    /**
     * 通过食物名称识别（文本输入）
     */
    public FoodRecognitionResult recognizeByName(Long userId, String foodName) {
        log.info("开始识别食物: {}", foodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 在数据库中搜索
            List<FoodNutrition> dbResults = foodNutritionRepository
                .fullTextSearch(foodName);
            
            List<FoodItem> foods = new ArrayList<>();
            
            if (!dbResults.isEmpty()) {
                // 数据库中找到了
                log.info("在数据库中找到 {} 条匹配记录", dbResults.size());
                for (FoodNutrition nutrition : dbResults) {
                    FoodItem item = FoodItem.builder()
                        .name(nutrition.getFoodName())
                        .confidence(0.95) // 数据库数据置信度高
                        .nutrition(FoodItem.NutritionInfo.builder()
                            .energy(nutrition.getEnergy().doubleValue())
                            .protein(nutrition.getProtein().doubleValue())
                            .carbohydrate(nutrition.getCarbohydrate().doubleValue())
                            .fat(nutrition.getFat().doubleValue())
                            .source("database")
                            .build())
                        .build();
                    foods.add(item);
                }
            } else {
                // 数据库中没找到，使用AI估算
                log.info("数据库中未找到，使用AI估算营养数据");
                FoodItem item = estimateNutritionByAI(foodName);
                foods.add(item);
            }
            
            long endTime = System.currentTimeMillis();
            
            FoodRecognitionResult result = FoodRecognitionResult.builder()
                .foods(foods)
                .totalCount(foods.size())
                .recognitionTime(endTime - startTime)
                .imageUrl(null)
                .build();
            
            // 保存识别历史
            saveRecognitionHistory(userId, "TEXT", foodName, result);
            
            log.info("食物识别完成，耗时: {}ms", result.getRecognitionTime());
            return result;
            
        } catch (Exception e) {
            log.error("食物识别失败", e);
            throw new RuntimeException("食物识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过图片识别 - 使用百度AI
     */
    public FoodRecognitionResult recognizeByImage(Long userId, MultipartFile image) {
        log.info("收到图片识别请求，文件大小: {} bytes", image.getSize());
        
        // 检查客户端是否初始化
        if (aipImageClassify == null) {
            throw new UnsupportedOperationException(
                "图片识别功能需要配置百度AI，请检查环境变量 BAIDU_APP_ID, BAIDU_API_KEY, BAIDU_SECRET_KEY"
            );
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 读取图片字节
            byte[] imageBytes = image.getBytes();
            
            // 2. 调用百度AI菜品识别
            HashMap<String, String> options = new HashMap<>();
            options.put("top_num", "5"); // 返回top5结果
            options.put("filter_threshold", "0.7"); // 置信度阈值
            options.put("baike_num", "0"); // 不返回百科信息
            
            JSONObject response = aipImageClassify.dishDetect(imageBytes, options);
            
            log.info("百度AI响应: {}", response.toString());
            
            // 3. 解析识别结果
            List<FoodItem> foods = new ArrayList<>();
            
            if (response.has("result")) {
                JSONArray resultArray = response.getJSONArray("result");
                
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject item = resultArray.getJSONObject(i);
                    String foodName = item.getString("name");
                    double probability = item.getDouble("probability");
                    
                    // 获取营养信息
                    FoodItem.NutritionInfo nutrition = getNutritionInfo(foodName);
                    
                    // 如果百度返回了卡路里信息
                    if (item.has("calorie")) {
                        String calorieStr = item.getString("calorie");
                        try {
                            double calorie = Double.parseDouble(calorieStr);
                            nutrition.setEnergy(calorie);
                        } catch (Exception e) {
                            log.warn("解析卡路里失败: {}", calorieStr);
                        }
                    }
                    
                    FoodItem foodItem = FoodItem.builder()
                        .name(foodName)
                        .confidence(probability)
                        .nutrition(nutrition)
                        .build();
                    
                    foods.add(foodItem);
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            FoodRecognitionResult result = FoodRecognitionResult.builder()
                .foods(foods)
                .totalCount(foods.size())
                .recognitionTime(endTime - startTime)
                .imageUrl(null)
                .build();
            
            // 保存识别历史
            saveRecognitionHistory(userId, "IMAGE", null, result);
            
            log.info("图片识别完成，识别到 {} 个食物，耗时: {}ms", foods.size(), result.getRecognitionTime());
            return result;
            
        } catch (Exception e) {
            log.error("图片识别失败", e);
            throw new RuntimeException("图片识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取营养信息（数据库或AI估算）
     */
    private FoodItem.NutritionInfo getNutritionInfo(String foodName) {
        // 先查数据库
        List<FoodNutrition> dbResults = foodNutritionRepository
            .fullTextSearch(foodName);
        
        if (!dbResults.isEmpty()) {
            FoodNutrition nutrition = dbResults.get(0);
            return FoodItem.NutritionInfo.builder()
                .energy(nutrition.getEnergy().doubleValue())
                .protein(nutrition.getProtein().doubleValue())
                .carbohydrate(nutrition.getCarbohydrate().doubleValue())
                .fat(nutrition.getFat().doubleValue())
                .source("database")
                .build();
        }
        
        // 数据库没有，使用AI估算
        FoodItem aiItem = estimateNutritionByAI(foodName);
        return aiItem.getNutrition();
    }
    
    /**
     * 使用AI估算营养数据
     */
    private FoodItem estimateNutritionByAI(String foodName) {
        log.info("使用AI估算食物营养数据: {}", foodName);
        
        String prompt = String.format("""
            请估算以下食物的营养成分（每100g）：%s
            
            请严格按照以下JSON格式返回，不要添加任何其他文字：
            {
              "energy": 热量(kcal),
              "protein": 蛋白质(g),
              "carbohydrate": 碳水化合物(g),
              "fat": 脂肪(g)
            }
            
            注意：
            1. 只返回JSON，不要有任何解释文字
            2. 数值必须是数字，不要带单位
            3. 如果不确定，给出合理的估算值
            """, foodName);
        
        try {
            String aiResponse = chatLanguageModel.generate(prompt);
            log.info("AI响应: {}", aiResponse);
            
            // 提取JSON部分
            String jsonStr = extractJson(aiResponse);
            
            // 解析JSON
            @SuppressWarnings("unchecked")
            var nutritionMap = objectMapper.readValue(jsonStr, java.util.Map.class);
            
            FoodItem.NutritionInfo nutrition = FoodItem.NutritionInfo.builder()
                .energy(getDoubleValue(nutritionMap.get("energy")))
                .protein(getDoubleValue(nutritionMap.get("protein")))
                .carbohydrate(getDoubleValue(nutritionMap.get("carbohydrate")))
                .fat(getDoubleValue(nutritionMap.get("fat")))
                .source("estimated")
                .build();
            
            return FoodItem.builder()
                .name(foodName)
                .confidence(0.75) // AI估算置信度较低
                .nutrition(nutrition)
                .build();
                
        } catch (Exception e) {
            log.error("AI估算失败", e);
            // 返回默认值
            return FoodItem.builder()
                .name(foodName)
                .confidence(0.5)
                .nutrition(FoodItem.NutritionInfo.builder()
                    .energy(100.0)
                    .protein(5.0)
                    .carbohydrate(15.0)
                    .fat(2.0)
                    .source("default")
                    .build())
                .build();
        }
    }
    
    /**
     * 从AI响应中提取JSON
     */
    private String extractJson(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return response.trim();
    }
    
    /**
     * 转换为Double值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * 保存识别历史
     */
    private void saveRecognitionHistory(Long userId, String recognitionType, String inputText, FoodRecognitionResult result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            
            FoodRecognitionHistory history = FoodRecognitionHistory.builder()
                .userId(userId)
                .recognitionType(recognitionType)
                .inputText(inputText)
                .imageUrl(result.getImageUrl())
                .recognitionResult(resultJson)
                .build();
            
            recognitionHistoryRepository.save(history);
            log.info("识别历史已保存: type={}, input={}", recognitionType, inputText);
            
        } catch (Exception e) {
            log.error("保存识别历史失败", e);
        }
    }
    
    /**
     * 获取识别历史
     */
    public List<FoodRecognitionHistory> getHistory(Long userId) {
        return recognitionHistoryRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 删除识别历史
     */
    public void deleteHistory(Long id, Long userId) {
        // 验证记录是否属于当前用户
        FoodRecognitionHistory history = recognitionHistoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("历史记录不存在"));
        
        if (!history.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此记录");
        }
        
        recognitionHistoryRepository.deleteById(id);
        log.info("用户 {} 删除了识别历史记录 {}", userId, id);
    }
}
