package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.entity.Merchant;
import com.nutriai.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台 - 商家管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Merchant>>> getMerchants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                merchantService.getMerchants(keyword, status, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Merchant>> getMerchant(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(merchantService.getMerchant(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Merchant>> createMerchant(@RequestBody Merchant merchant) {
        return ResponseEntity.ok(ApiResponse.success("创建成功", merchantService.createMerchant(merchant)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Merchant>> updateMerchant(
            @PathVariable Long id, @RequestBody Merchant update) {
        return ResponseEntity.ok(ApiResponse.success("更新成功", merchantService.updateMerchant(id, update)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Merchant>> updateMerchantStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        Merchant update = new Merchant();
        update.setStatus(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("状态更新成功",
                merchantService.updateMerchant(id, update)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMerchantStats() {
        return ResponseEntity.ok(ApiResponse.success(merchantService.getMerchantStats()));
    }
}
