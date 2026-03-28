package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.dto.FeedbackDTO;
import com.nutriai.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 用户提交反馈
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackDTO>> submitFeedback(
            HttpServletRequest request,
            @RequestBody FeedbackDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        FeedbackDTO result = feedbackService.submitFeedback(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("反馈提交成功", result));
    }

    /**
     * 获取用户自己的反馈列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getMyFeedbacks(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<FeedbackDTO> result = feedbackService.getUserFeedbacks(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ========== 管理员接口 ==========

    /**
     * 管理员获取所有反馈
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<FeedbackDTO>>> getAllFeedbacks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FeedbackDTO> result = feedbackService.getAllFeedbacks(status, type, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 管理员回复反馈
     */
    @PostMapping("/admin/{id}/reply")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackDTO>> replyFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        String status = body.get("status");
        FeedbackDTO result = feedbackService.replyFeedback(id, reply, status);
        return ResponseEntity.ok(ApiResponse.success("回复成功", result));
    }

    /**
     * 管理员更新反馈状态
     */
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        FeedbackDTO result = feedbackService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", result));
    }

    /**
     * 管理员获取反馈统计
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        Map<String, Long> stats = feedbackService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
