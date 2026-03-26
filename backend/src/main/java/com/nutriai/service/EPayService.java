package com.nutriai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriai.config.EPayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 易支付（EPay）第三方聚合支付服务
 * 协议文档：标准易支付接口（MD5签名）
 * 支持：支付宝、微信、QQ钱包等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EPayService {

    private final EPayConfig ePayConfig;
    private final ObjectMapper objectMapper;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 检查易支付是否已配置
     */
    public boolean isConfigured() {
        return ePayConfig.getMerchantId() != null && !ePayConfig.getMerchantId().isBlank()
                && ePayConfig.getMerchantKey() != null && !ePayConfig.getMerchantKey().isBlank()
                && ePayConfig.getSubmitUrl() != null && !ePayConfig.getSubmitUrl().isBlank();
    }

    /**
     * 构建支付跳转URL（前端直接 window.open 打开）
     *
     * @param orderNo 业务订单号
     * @param amount  金额（元）
     * @param subject 商品名称
     * @param payType 支付方式：alipay/wxpay/qqpay
     * @return 完整的支付跳转URL
     */
    public String buildPayUrl(String orderNo, BigDecimal amount, String subject, String payType) {
        String type = (payType != null && !payType.isBlank()) ? payType : ePayConfig.getDefaultType();

        // 构建签名参数（按易支付协议，参与签名的字段按key的ASCII排序）
        Map<String, String> params = new TreeMap<>();
        params.put("pid", ePayConfig.getMerchantId());
        params.put("type", type);
        params.put("out_trade_no", orderNo);
        params.put("notify_url", ePayConfig.getNotifyUrl());
        params.put("return_url", ePayConfig.getReturnUrl());
        params.put("name", subject);
        params.put("money", amount.toPlainString());

        // 生成签名
        String sign = generateSign(params);

        // 构建完整URL
        StringBuilder url = new StringBuilder(ePayConfig.getSubmitUrl());
        url.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            url.append("=");
            url.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            url.append("&");
        }
        url.append("sign=").append(sign);
        url.append("&sign_type=MD5");

        String payUrl = url.toString();
        log.info("易支付URL构建成功, orderNo={}, type={}, amount={}", orderNo, type, amount);
        return payUrl;
    }

    /**
     * 验证异步通知签名
     *
     * @param params 回调参数（不含sign和sign_type）
     * @param sign   回调中的sign值
     * @return true=签名合法
     */
    public boolean verifyNotifySign(Map<String, String> params, String sign) {
        if (sign == null || sign.isBlank()) {
            log.warn("易支付回调缺少sign参数");
            return false;
        }

        // 过滤空值及sign/sign_type字段
        Map<String, String> filteredParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("sign".equals(key) || "sign_type".equals(key)) continue;
            if (value != null && !value.isBlank()) {
                filteredParams.put(key, value);
            }
        }

        String expectedSign = generateSign(filteredParams);
        boolean result = expectedSign.equalsIgnoreCase(sign);
        if (!result) {
            log.warn("易支付回调签名验证失败: expected={}, actual={}", expectedSign, sign);
        }
        return result;
    }

    /**
     * 主动查询订单状态（通过MAPI接口）
     *
     * @param orderNo 业务订单号
     * @return 订单状态：TRADE_SUCCESS / TRADE_CLOSED / null(查询失败)
     */
    public String queryTradeStatus(String orderNo) {
        if (ePayConfig.getMapiUrl() == null || ePayConfig.getMapiUrl().isBlank()) {
            log.warn("易支付MAPI接口未配置，无法查询订单");
            return null;
        }

        try {
            String url = ePayConfig.getMapiUrl()
                    + "?act=order"
                    + "&pid=" + URLEncoder.encode(ePayConfig.getMerchantId(), StandardCharsets.UTF_8)
                    + "&key=" + URLEncoder.encode(ePayConfig.getMerchantKey(), StandardCharsets.UTF_8)
                    + "&out_trade_no=" + URLEncoder.encode(orderNo, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            log.info("易支付查单响应, orderNo={}: {}", orderNo, body);

            JsonNode json = objectMapper.readTree(body);
            if (json.has("code") && json.get("code").asInt() == 1) {
                return json.path("status").asText(null);
            } else {
                log.warn("易支付查单返回错误, orderNo={}: {}", orderNo, body);
                return null;
            }
        } catch (Exception e) {
            log.error("易支付查单异常, orderNo={}", orderNo, e);
            return null;
        }
    }

    /**
     * 生成MD5签名
     * 规则：按key的ASCII排序拼接 key=value&，最后追加商户密钥，取MD5
     */
    private String generateSign(Map<String, String> params) {
        // TreeMap保证ASCII排序
        String signStr = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        signStr += ePayConfig.getMerchantKey();

        return md5(signStr);
    }

    /**
     * MD5哈希
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }
}
