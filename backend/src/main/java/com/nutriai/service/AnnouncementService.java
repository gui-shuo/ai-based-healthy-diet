package com.nutriai.service;

import com.nutriai.dto.admin.SystemAnnouncementDTO;
import com.nutriai.entity.SystemAnnouncement;
import com.nutriai.repository.SystemAnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 公告服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {
    
    private final SystemAnnouncementRepository announcementRepository;
    
    /**
     * 获取公开的公告列表（分页）
     * 只返回启用的公告（不限制时间范围，因为时间字段可能为NULL）
     */
    public Page<SystemAnnouncementDTO> getPublicAnnouncements(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "priority", "createdAt"));
        
        log.info("查询公告列表: page={}, size={}", page, size);
        
        // 简化查询：只查询启用的公告，不限制时间
        Page<SystemAnnouncement> announcementPage = announcementRepository
                .findByIsActiveTrueOrderByPriorityDescCreatedAtDesc(pageable);
        
        log.info("查询到 {} 条公告", announcementPage.getTotalElements());
        
        return announcementPage.map(this::convertToDTO);
    }
    
    /**
     * 根据ID获取公告
     */
    public SystemAnnouncementDTO getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    /**
     * 转换为DTO
     */
    private SystemAnnouncementDTO convertToDTO(SystemAnnouncement announcement) {
        return SystemAnnouncementDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .priority(announcement.getPriority())
                .isActive(announcement.getIsActive())
                .startTime(announcement.getStartTime())
                .endTime(announcement.getEndTime())
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}
