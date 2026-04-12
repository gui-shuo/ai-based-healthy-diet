package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.entity.Nutritionist;
import com.nutriai.entity.NutritionProduct;
import com.nutriai.repository.NutritionistRepository;
import com.nutriai.repository.NutritionProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台 - 营养师和产品管理
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminShopController {

    private final NutritionistRepository nutritionistRepository;
    private final NutritionProductRepository productRepository;

    // ==================== 营养师管理 ====================

    @GetMapping("/nutritionists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Nutritionist>>> getNutritionists(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Nutritionist> result;
        if (keyword != null && !keyword.isBlank()) {
            result = nutritionistRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        } else {
            result = nutritionistRepository.findAllByOrderBySortOrderAsc(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/nutritionists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Nutritionist>> getNutritionist(@PathVariable Long id) {
        Nutritionist n = nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("营养师不存在"));
        return ResponseEntity.ok(ApiResponse.success(n));
    }

    @PostMapping("/nutritionists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Nutritionist>> createNutritionist(@RequestBody Nutritionist nutritionist) {
        Nutritionist saved = nutritionistRepository.save(nutritionist);
        log.info("创建营养师: {}", saved.getName());
        return ResponseEntity.ok(ApiResponse.success("创建成功", saved));
    }

    @PutMapping("/nutritionists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Nutritionist>> updateNutritionist(
            @PathVariable Long id, @RequestBody Nutritionist update) {
        Nutritionist existing = nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("营养师不存在"));

        if (update.getName() != null) existing.setName(update.getName());
        if (update.getAvatar() != null) existing.setAvatar(update.getAvatar());
        if (update.getTitle() != null) existing.setTitle(update.getTitle());
        if (update.getSpecialties() != null) existing.setSpecialties(update.getSpecialties());
        if (update.getIntroduction() != null) existing.setIntroduction(update.getIntroduction());
        if (update.getExperienceYears() != null) existing.setExperienceYears(update.getExperienceYears());
        if (update.getConsultationFee() != null) existing.setConsultationFee(update.getConsultationFee());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getWorkingHours() != null) existing.setWorkingHours(update.getWorkingHours());
        if (update.getIsActive() != null) existing.setIsActive(update.getIsActive());
        if (update.getSortOrder() != null) existing.setSortOrder(update.getSortOrder());

        Nutritionist saved = nutritionistRepository.save(existing);
        log.info("更新营养师: {} (id={})", saved.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("更新成功", saved));
    }

    @DeleteMapping("/nutritionists/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteNutritionist(@PathVariable Long id) {
        nutritionistRepository.deleteById(id);
        log.info("删除营养师: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/nutritionists/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Nutritionist>> updateNutritionistStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Nutritionist n = nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("营养师不存在"));
        if (body.containsKey("status")) n.setStatus((String) body.get("status"));
        if (body.containsKey("isActive")) n.setIsActive((Boolean) body.get("isActive"));
        Nutritionist saved = nutritionistRepository.save(n);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", saved));
    }

    // ==================== 产品管理 ====================

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<NutritionProduct>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<NutritionProduct> result;
        if (keyword != null && !keyword.isBlank()) {
            result = productRepository.adminSearchByKeyword(keyword, PageRequest.of(page, size));
        } else if (category != null && !category.isBlank()) {
            result = productRepository.findByCategoryOrderBySortOrderAsc(category, PageRequest.of(page, size));
        } else {
            result = productRepository.findAllByOrderBySortOrderAsc(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NutritionProduct>> getProduct(@PathVariable Long id) {
        NutritionProduct p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        return ResponseEntity.ok(ApiResponse.success(p));
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NutritionProduct>> createProduct(@RequestBody NutritionProduct product) {
        NutritionProduct saved = productRepository.save(product);
        log.info("创建产品: {}", saved.getName());
        return ResponseEntity.ok(ApiResponse.success("创建成功", saved));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NutritionProduct>> updateProduct(
            @PathVariable Long id, @RequestBody NutritionProduct update) {
        NutritionProduct existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        if (update.getName() != null) existing.setName(update.getName());
        if (update.getImageUrl() != null) existing.setImageUrl(update.getImageUrl());
        if (update.getImageUrls() != null) existing.setImageUrls(update.getImageUrls());
        if (update.getCategory() != null) existing.setCategory(update.getCategory());
        if (update.getBrand() != null) existing.setBrand(update.getBrand());
        if (update.getBrief() != null) existing.setBrief(update.getBrief());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getOriginalPrice() != null) existing.setOriginalPrice(update.getOriginalPrice());
        if (update.getSalePrice() != null) existing.setSalePrice(update.getSalePrice());
        if (update.getStock() != null) existing.setStock(update.getStock());
        if (update.getTags() != null) existing.setTags(update.getTags());
        if (update.getSpecifications() != null) existing.setSpecifications(update.getSpecifications());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getIsRecommended() != null) existing.setIsRecommended(update.getIsRecommended());
        if (update.getSortOrder() != null) existing.setSortOrder(update.getSortOrder());

        NutritionProduct saved = productRepository.save(existing);
        log.info("更新产品: {} (id={})", saved.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("更新成功", saved));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        log.info("删除产品: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/products/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NutritionProduct>> updateProductStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        NutritionProduct p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        p.setStatus(body.get("status"));
        NutritionProduct saved = productRepository.save(p);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", saved));
    }
}
