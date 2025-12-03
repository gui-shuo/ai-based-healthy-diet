package com.nutriai.dto.user;

import com.nutriai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户资料响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginTime;
    
    /**
     * 从User实体转换
     */
    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
}
