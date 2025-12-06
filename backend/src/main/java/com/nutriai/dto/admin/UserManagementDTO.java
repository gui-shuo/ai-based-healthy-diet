package com.nutriai.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户管理DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String status;              // active/disabled
    private String role;                // USER/ADMIN/SUPER_ADMIN
    private String memberLevel;         // FREE/BRONZE/SILVER/GOLD
    private Integer growthValue;
    private LocalDateTime memberExpireTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private Long totalChats;            // 总对话数
    private Long todayChats;            // 今日对话数
}
