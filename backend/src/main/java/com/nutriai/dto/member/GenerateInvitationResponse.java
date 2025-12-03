package com.nutriai.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成邀请链接响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInvitationResponse {
    
    /**
     * 邀请码
     */
    private String invitationCode;
    
    /**
     * 完整邀请链接
     */
    private String invitationLink;
    
    /**
     * 二维码URL（可选）
     */
    private String qrCodeUrl;
    
    /**
     * 邀请文案
     */
    private String invitationText;
}
