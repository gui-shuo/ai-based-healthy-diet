package com.nutriai.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadAvatarResponse {
    
    private String avatarUrl;   // 头像URL
    private String fileName;    // 文件名
    private Long fileSize;      // 文件大小（字节）
}
