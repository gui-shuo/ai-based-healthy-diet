package com.nutriai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 易支付（EPay）第三方支付配置
 * 支持支付宝/微信/QQ钱包等聚合支付
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "epay")
public class EPayConfig {

    /** 商户ID */
    private String merchantId;

    /** 商户密钥（用于MD5签名） */
    private String merchantKey;

    /** API地址（查询等接口） */
    private String apiUrl;

    /** 支付提交地址（跳转到收银台） */
    private String submitUrl;

    /** MAPI接口地址（订单查询） */
    private String mapiUrl;

    /** 异步通知地址（支付成功后服务端回调） */
    private String notifyUrl;

    /** 同步跳转地址（用户支付完成后浏览器跳转） */
    private String returnUrl;

    /** 默认支付方式：alipay/wxpay/qqpay */
    private String defaultType = "alipay";
}
