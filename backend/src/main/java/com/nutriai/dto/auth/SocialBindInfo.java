package com.nutriai.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 社交账号绑定状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialBindInfo {
    
    private boolean wechatBound;
    private String wechatNickname;
    
    private boolean qqBound;
    private String qqNickname;

    /** APP端需要补充QQ验证（有web openId但缺app openId） */
    private boolean qqNeedAppVerify;

    /** Web端需要补充QQ验证（有app openId但缺web openId） */
    private boolean qqNeedWebVerify;
}
