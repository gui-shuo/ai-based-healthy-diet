package com.nutriai.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "邮箱不能为空")
    private String username; // 前端传邮箱，后端兼容邮箱或用户名
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 验证码（登录失败3次后需要）
     */
    private String captcha;
    
    /**
     * 验证码Key
     */
    private String captchaKey;
    
    /**
     * 记住我（7天免登录）
     */
    private Boolean rememberMe = false;
}
