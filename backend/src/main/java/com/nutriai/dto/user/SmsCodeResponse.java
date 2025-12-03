package com.nutriai.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信验证码响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCodeResponse {
    
    private String smsKey;      // 短信验证码Key
    private Long expiresIn;     // 过期时间（秒）
    private String message;     // 提示信息
}
