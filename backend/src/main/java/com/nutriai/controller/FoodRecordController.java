package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.dto.food.CreateFoodRecordRequest;
import com.nutriai.dto.food.FoodRecordResponse;
import com.nutriai.dto.food.NutritionStatsResponse;
import com.nutriai.entity.FoodRecord;
import com.nutriai.service.FoodRecordService;
import com.nutriai.service.OssService;
import com.nutriai.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 饮食记录Controller
 */
@Slf4j
@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodRecordController {
    
    private final FoodRecordService foodRecordService;
    private final OssService ossService;
    private final JwtUtil jwtUtil;
    
    /**
     * 创建饮食记录
     * POST /api/food/records
     */
    @PostMapping("/records")
    public ApiResponse<FoodRecordResponse> createFoodRecord(
            @Valid @RequestBody CreateFoodRecordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        FoodRecordResponse response = foodRecordService.createFoodRecord(userId, request);
        return ApiResponse.success("创建成功", response);
    }
    
    /**
     * 查询饮食记录（分页）
     * GET /api/food/records?page=0&size=10&startDate=2024-01-01&endDate=2024-01-31&mealType=BREAKFAST
     */
    @GetMapping("/records")
    public ApiResponse<Page<FoodRecordResponse>> getFoodRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) FoodRecord.MealType mealType,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        Page<FoodRecordResponse> records = foodRecordService.getFoodRecords(
                userId, page, size, startDate, endDate, mealType);
        return ApiResponse.success(records);
    }
    
    /**
     * 获取饮食记录详情
     * GET /api/food/records/{id}
     */
    @GetMapping("/records/{id}")
    public ApiResponse<FoodRecordResponse> getFoodRecord(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        FoodRecordResponse record = foodRecordService.getFoodRecordById(userId, id);
        return ApiResponse.success(record);
    }
    
    /**
     * 删除饮食记录
     * DELETE /api/food/records/{id}
     */
    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteFoodRecord(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        foodRecordService.deleteFoodRecord(userId, id);
        ApiResponse<Void> response = ApiResponse.success();
        response.setMessage("删除成功");
        return response;
    }
    
    /**
     * 获取营养摄入统计
     * GET /api/food/stats?date=2024-01-01
     */
    @GetMapping("/stats")
    public ApiResponse<NutritionStatsResponse> getNutritionStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        // 如果未指定日期，默认查询今天
        if (date == null) {
            date = LocalDate.now();
        }
        NutritionStatsResponse stats = foodRecordService.getNutritionStats(userId, date);
        return ApiResponse.success(stats);
    }
    
    /**
     * 上传食物照片
     * POST /api/food/photo
     */
    @PostMapping("/photo")
    public ApiResponse<String> uploadFoodPhoto(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        log.info("上传食物照片: userId={}, filename={}", userId, file.getOriginalFilename());
        
        // 调用OSS服务上传文件（复用头像上传逻辑，或创建专门的食物照片上传方法）
        String photoUrl = ossService.uploadFoodPhoto(file);
        
        return ApiResponse.success("上传成功", photoUrl);
    }
    
    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return jwtUtil.getUserIdFromToken(token);
    }
    
    /**
     * 从请求头中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
