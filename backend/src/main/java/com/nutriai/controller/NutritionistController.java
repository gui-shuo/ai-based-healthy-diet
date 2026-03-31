package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.entity.ConsultationOrder;
import com.nutriai.entity.Nutritionist;
import com.nutriai.service.ConsultationService;
import com.nutriai.repository.NutritionistRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nutritionist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NUTRITIONIST')")
public class NutritionistController {

    private final ConsultationService consultationService;
    private final NutritionistRepository nutritionistRepository;

    private Long getUserIdFromToken(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) throw new RuntimeException("未登录");
        return Long.parseLong(userId.toString());
    }

    /** Get the nutritionist's own profile (linked via userId) */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Nutritionist>> getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        Nutritionist n = nutritionistRepository.findByUserId(userId)
                .orElse(null);
        if (n == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "未找到关联的营养师资料，请联系管理员"));
        }
        return ResponseEntity.ok(ApiResponse.success(n));
    }

    /** Get consultations for this nutritionist */
    @GetMapping("/consultations")
    public ResponseEntity<ApiResponse<Page<ConsultationOrder>>> getConsultations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Long userId = getUserIdFromToken(request);
        Nutritionist n = nutritionistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("未关联营养师资料"));
        Page<ConsultationOrder> orders = consultationService.getNutritionistConsultations(n.getId(), page, size, status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /** Nutritionist sends a reply message to a consultation */
    @PostMapping("/consultations/{orderNo}/reply")
    public ResponseEntity<ApiResponse<ConsultationOrder>> sendReply(
            HttpServletRequest request,
            @PathVariable String orderNo,
            @RequestBody Map<String, String> body) {
        Long userId = getUserIdFromToken(request);
        Nutritionist n = nutritionistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("未关联营养师资料"));
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "回复内容不能为空"));
        }
        ConsultationOrder order = consultationService.nutritionistReply(n.getId(), orderNo, content);
        return ResponseEntity.ok(ApiResponse.success("回复成功", order));
    }

    /** Get active (WAITING / IN_PROGRESS) consultations */
    @GetMapping("/consultations/active")
    public ResponseEntity<ApiResponse<List<ConsultationOrder>>> getActiveConsultations(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        Nutritionist n = nutritionistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("未关联营养师资料"));
        List<ConsultationOrder> orders = consultationService.getNutritionistActiveConsultations(n.getId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /** Update nutritionist's own status (ONLINE/OFFLINE/BUSY) */
    @PutMapping("/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = getUserIdFromToken(request);
        Nutritionist n = nutritionistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("未关联营养师资料"));
        String status = body.get("status");
        if (status == null || !status.matches("ONLINE|OFFLINE|BUSY")) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "无效状态"));
        }
        n.setStatus(status);
        nutritionistRepository.save(n);
        return ResponseEntity.ok(ApiResponse.success("状态已更新", null));
    }
}
