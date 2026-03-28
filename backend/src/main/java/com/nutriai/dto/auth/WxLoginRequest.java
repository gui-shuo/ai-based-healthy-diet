package com.nutriai.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求DTO
 */
@Data
public class WxLoginRequest {
    
    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
