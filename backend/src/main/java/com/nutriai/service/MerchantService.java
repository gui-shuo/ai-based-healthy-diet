package com.nutriai.service;

import com.nutriai.entity.Merchant;
import com.nutriai.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public Page<Merchant> getMerchants(String keyword, String status, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return merchantRepository.searchByKeyword(keyword, pageable);
        }
        if (status != null && !status.isBlank()) {
            return merchantRepository.findByStatusOrderBySortOrderAsc(status, pageable);
        }
        return merchantRepository.findAllByOrderBySortOrderAsc(pageable);
    }

    public Merchant getMerchant(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
    }

    @Transactional
    public Merchant createMerchant(Merchant merchant) {
        Merchant saved = merchantRepository.save(merchant);
        log.info("创建商家: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    @Transactional
    public Merchant updateMerchant(Long id, Merchant update) {
        Merchant existing = getMerchant(id);
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getLogo() != null) existing.setLogo(update.getLogo());
        if (update.getPhone() != null) existing.setPhone(update.getPhone());
        if (update.getAddress() != null) existing.setAddress(update.getAddress());
        if (update.getLatitude() != null) existing.setLatitude(update.getLatitude());
        if (update.getLongitude() != null) existing.setLongitude(update.getLongitude());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getBusinessHours() != null) existing.setBusinessHours(update.getBusinessHours());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getType() != null) existing.setType(update.getType());
        if (update.getOwnerId() != null) existing.setOwnerId(update.getOwnerId());
        if (update.getSortOrder() != null) existing.setSortOrder(update.getSortOrder());
        Merchant saved = merchantRepository.save(existing);
        log.info("更新商家: {} (id={})", saved.getName(), id);
        return saved;
    }

    @Transactional
    public void deleteMerchant(Long id) {
        merchantRepository.deleteById(id);
        log.info("删除商家: id={}", id);
    }

    public Map<String, Object> getMerchantStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", merchantRepository.count());
        stats.put("active", merchantRepository.countByStatus("ACTIVE"));
        stats.put("inactive", merchantRepository.countByStatus("INACTIVE"));
        return stats;
    }
}
