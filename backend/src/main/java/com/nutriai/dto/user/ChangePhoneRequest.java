package com.nutriai.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改手机号请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePhoneRequest {
    
    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String newPhone;

    /** 新版邮箱验证码 */
    private String emailCode;

    /** 旧版短信验证码（向后兼容） */
    private String smsCode;

    /** 旧版短信验证码Key（向后兼容） */
    private String smsKey;
}
