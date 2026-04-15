package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信支付Controller（预留接口，待微信支付审核通过后实现）
 */
@Slf4j
@RestController
@RequestMapping("/wxpay")
@RequiredArgsConstructor
public class WxPayController {

    /**
     * 微信支付统一下单（预留接口）
     */
    @PostMapping("/unified-order")
    public ApiResponse<Map<String, String>> unifiedOrder(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        // TODO: 接入微信支付后实现
        log.info("微信支付统一下单请求: {}", body);
        return ApiResponse.error(503, "微信支付正在审核中，请使用模拟支付");
    }

    /**
     * 微信支付回调（预留）
     */
    @PostMapping("/notify")
    public String payNotify(@RequestBody String xmlData) {
        log.info("收到微信支付回调");
        // TODO: 实现支付回调验签和订单状态更新
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/query/{orderNo}")
    public ApiResponse<Map<String, Object>> queryPayment(@PathVariable String orderNo) {
        log.info("查询支付状态: {}", orderNo);
        return ApiResponse.error(503, "微信支付正在审核中");
    }
}
