package com.nutriai.service;

import com.nutriai.dto.admin.SystemAnnouncementDTO;
import com.nutriai.dto.admin.SystemConfigDTO;
import com.nutriai.entity.SystemAnnouncement;
import com.nutriai.entity.SystemConfig;
import com.nutriai.entity.User;
import com.nutriai.repository.SystemAnnouncementRepository;
import com.nutriai.repository.SystemConfigRepository;
import com.nutriai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 管理后台系统配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminConfigService {
    
    private final SystemConfigRepository configRepository;
    private final SystemAnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    
    /**
     * 获取所有配置
     */
    public List<SystemConfigDTO> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(this::convertConfigToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据分类获取配置
     */
    public List<SystemConfigDTO> getConfigsByCategory(String category) {
        return configRepository.findByCategory(category).stream()
                .map(this::convertConfigToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取单个配置
     */
    public SystemConfigDTO getConfigByKey(String key) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        return convertConfigToDTO(config);
    }
    
    /**
     * 更新配置
     */
    @Transactional
    public void updateConfig(String key, String value) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        config.setConfigValue(value);
        configRepository.save(config);
        
        log.info("更新系统配置: key={}, value={}", key, value);
    }
    
    /**
     * 创建配置
     */
    @Transactional
    public SystemConfigDTO createConfig(SystemConfigDTO dto) {
        if (configRepository.existsByConfigKey(dto.getConfigKey())) {
            throw new RuntimeException("配置键已存在");
        }
        
        SystemConfig config = SystemConfig.builder()
                .configKey(dto.getConfigKey())
                .configValue(dto.getConfigValue())
                .configType(dto.getConfigType())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .isPublic(dto.getIsPublic())
                .build();
        
        config = configRepository.save(config);
        log.info("创建系统配置: key={}", dto.getConfigKey());
        
        return convertConfigToDTO(config);
    }
    
    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(String key) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        configRepository.delete(config);
        log.info("删除系统配置: key={}", key);
    }
    
    /**
     * 获取公开配置值
     */
    public String getPublicConfigValue(String key) {
        Optional<SystemConfig> configOpt = configRepository.findByConfigKey(key);
        
        if (configOpt.isPresent()) {
            SystemConfig config = configOpt.get();
            if (config.getIsPublic() != null && config.getIsPublic()) {
                return config.getConfigValue();
            }
        }
        
        return null;
    }
    
    /**
     * 获取所有公告
     */
    public List<SystemAnnouncementDTO> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByPriorityDescCreatedAtDesc().stream()
                .map(this::convertAnnouncementToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取当前有效的公告
     */
    public List<SystemAnnouncementDTO> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now()).stream()
                .map(this::convertAnnouncementToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建公告
     */
    @Transactional
    public SystemAnnouncementDTO createAnnouncement(SystemAnnouncementDTO dto, Long adminId) {
        SystemAnnouncement announcement = SystemAnnouncement.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(dto.getType())
                .priority(dto.getPriority())
                .isActive(dto.getIsActive())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .createdBy(adminId)
                .build();
        
        announcement = announcementRepository.save(announcement);
        log.info("创建系统公告: id={}, title={}", announcement.getId(), announcement.getTitle());
        
        return convertAnnouncementToDTO(announcement);
    }
    
    /**
     * 更新公告
     */
    @Transactional
    public SystemAnnouncementDTO updateAnnouncement(Long id, SystemAnnouncementDTO dto) {
        SystemAnnouncement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        announcement.setPriority(dto.getPriority());
        announcement.setIsActive(dto.getIsActive());
        announcement.setStartTime(dto.getStartTime());
        announcement.setEndTime(dto.getEndTime());
        
        announcement = announcementRepository.save(announcement);
        log.info("更新系统公告: id={}", id);
        
        return convertAnnouncementToDTO(announcement);
    }
    
    /**
     * 删除公告
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
        log.info("删除系统公告: id={}", id);
    }
    
    /**
     * 转换配置为DTO
     */
    private SystemConfigDTO convertConfigToDTO(SystemConfig config) {
        return SystemConfigDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .configType(config.getConfigType())
                .description(config.getDescription())
                .category(config.getCategory())
                .isPublic(config.getIsPublic())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
    
    /**
     * 转换公告为DTO
     */
    private SystemAnnouncementDTO convertAnnouncementToDTO(SystemAnnouncement announcement) {
        String createdByName = null;
        if (announcement.getCreatedBy() != null) {
            Optional<User> userOpt = userRepository.findById(announcement.getCreatedBy());
            if (userOpt.isPresent()) {
                createdByName = userOpt.get().getUsername();
            }
        }
        
        return SystemAnnouncementDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .priority(announcement.getPriority())
                .isActive(announcement.getIsActive())
                .startTime(announcement.getStartTime())
                .endTime(announcement.getEndTime())
                .createdBy(announcement.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}
