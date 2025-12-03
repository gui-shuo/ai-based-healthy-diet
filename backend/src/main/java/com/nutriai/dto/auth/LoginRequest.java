package com.nutriai.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
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
