package com.nutriai.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户资料请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    private String avatar;
}
