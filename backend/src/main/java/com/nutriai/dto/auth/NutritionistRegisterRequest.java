package com.nutriai.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 营养师注册申请DTO
 */
@Data
public class NutritionistRegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "邮箱验证码为6位数字")
    private String emailCode;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;

    // ---- 营养师专业信息 ----

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String name;

    @NotBlank(message = "职称不能为空")
    private String title; // 初级营养师/中级营养师/高级营养师/注册营养师

    private List<String> specialties; // 专业领域

    @Size(max = 2000, message = "简介不能超过2000个字符")
    private String introduction;

    @Min(value = 0, message = "从业年限不能为负数")
    private Integer experienceYears;

    @NotNull(message = "咨询费不能为空")
    @DecimalMin(value = "0.00", message = "咨询费不能为负数")
    private BigDecimal consultationFee;

    private String workingHours;

    /** 资质证书URL列表 */
    private List<String> certificateUrls;

    private String username; // 选填

    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
