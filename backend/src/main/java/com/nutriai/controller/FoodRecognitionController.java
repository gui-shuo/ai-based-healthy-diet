package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.dto.FoodRecognitionResult;
import com.nutriai.entity.FoodRecognitionHistory;
import com.nutriai.service.FoodRecognitionServiceV2;
import com.nutriai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 食物识别Controller
 */
@Slf4j
@RestController
@RequestMapping("/food-recognition")
@RequiredArgsConstructor
public class FoodRecognitionController {
    
    private final FoodRecognitionServiceV2 recognitionService;
    private final JwtUtil jwtUtil;
    
    /**
     * 通过食物名称识别（文本输入）
     */
    @PostMapping("/recognize-by-name")
    public ResponseEntity<ApiResponse<FoodRecognitionResult>> recognizeByName(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("foodName") String foodName) {
        
        log.info("收到食物名称识别请求: {}", foodName);
        
        try {
            // 验证用户身份
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 识别食物
            FoodRecognitionResult result = recognitionService.recognizeByName(userId, foodName);
            
            return ResponseEntity.ok(ApiResponse.success("识别成功", result));
            
        } catch (Exception e) {
            log.error("食物识别失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "识别失败: " + e.getMessage()));
        }
    }
    
    /**
     * 通过图片识别（预留接口）
     */
    @PostMapping("/recognize-by-image")
    public ResponseEntity<ApiResponse<FoodRecognitionResult>> recognizeByImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("image") MultipartFile image) {
        
        log.info("收到图片识别请求，文件大小: {} bytes", image.getSize());
        
        try {
            // 验证文件
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "图片不能为空"));
            }
            
            // 验证文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "只支持图片格式"));
            }
            
            // 验证文件大小（最大5MB）
            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "图片大小不能超过5MB"));
            }
            
            // 验证用户身份
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 识别食物
            FoodRecognitionResult result = recognitionService.recognizeByImage(userId, image);
            
            return ResponseEntity.ok(ApiResponse.success("识别成功", result));
            
        } catch (UnsupportedOperationException e) {
            log.warn("图片识别功能未启用: {}", e.getMessage());
            return ResponseEntity.status(501)
                .body(ApiResponse.error(501, e.getMessage()));
                
        } catch (Exception e) {
            log.error("图片识别失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "识别失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取识别历史
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<FoodRecognitionHistory>>> getHistory(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            List<FoodRecognitionHistory> history = recognitionService.getHistory(userId);
            
            return ResponseEntity.ok(ApiResponse.success(history));
            
        } catch (Exception e) {
            log.error("获取识别历史失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "获取历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除识别历史记录
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHistory(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            recognitionService.deleteHistory(id, userId);
            
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
            
        } catch (Exception e) {
            log.error("删除识别历史失败", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "删除失败: " + e.getMessage()));
        }
    }
}
