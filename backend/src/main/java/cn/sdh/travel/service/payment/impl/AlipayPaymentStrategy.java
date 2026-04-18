package cn.sdh.travel.service.payment.impl;

import cn.sdh.travel.common.enums.PaymentMethod;
import cn.sdh.travel.config.AlipayConfig;
import cn.sdh.travel.entity.domain.PaymentOrder;
import cn.sdh.travel.service.payment.PaymentStrategy;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付策略实现
 * 使用当面付（扫码支付）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentStrategy implements PaymentStrategy {

    private final AlipayConfig alipayConfig;

    @Autowired(required = false)
    private AlipayClient alipayClient;

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.ALIPAY;
    }

    @Override
    public void createOrder(PaymentOrder order) {
        if (!isConfigured()) {
            log.warn("支付宝未配置，创建模拟支付链接");
            String mockQrCodeUrl = "https://qr.alipay.com/mock_" + order.getOrderNo();
            order.setQrCodeUrl(mockQrCodeUrl);
            return;
        }

        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getOrderNo());
            bizContent.put("total_amount", new BigDecimal(order.getAmount()).divide(new BigDecimal(100)).setScale(2));
            bizContent.put("subject", order.getMemberLevel().equals("PRO") ? "TravelAI专业版会员" : "TravelAI企业版会员");
            bizContent.put("timeout_express", "30m");
            request.setBizContent(bizContent.toJSONString());

            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                order.setQrCodeUrl(response.getQrCode());
                log.info("支付宝订单创建成功: orderNo={}", order.getOrderNo());
            } else {
                log.error("支付宝订单创建失败: code={}, msg={}", response.getCode(), response.getMsg());
                throw new RuntimeException("创建支付宝订单失败: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("创建支付宝订单异常: orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("创建支付宝订单失败: " + e.getMessage());
        }
    }

    @Override
    public CallbackResult handleCallback(String body, Map<String, String> headers) {
        if (!isConfigured()) {
            log.warn("支付宝未配置，无法处理回调");
            return CallbackResult.fail("支付宝未配置");
        }

        try {
            // 解析回调参数（body是application/x-www-form-urlencoded格式）
            Map<String, String> params = parseFormParams(body);

            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!signVerified) {
                log.warn("支付宝回调签名验证失败");
                return CallbackResult.fail("签名验证失败");
            }

            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                return CallbackResult.success(outTradeNo, tradeNo);
            }

            return CallbackResult.fail("支付未完成: " + tradeStatus);
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return CallbackResult.fail("处理失败: " + e.getMessage());
        }
    }

    @Override
    public boolean queryPayStatus(PaymentOrder order) {
        if (!isConfigured()) {
            return false;
        }

        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getOrderNo());
            request.setBizContent(bizContent.toJSONString());

            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            }

            return false;
        } catch (AlipayApiException e) {
            log.warn("查询支付宝订单状态失败: orderNo={}", order.getOrderNo(), e);
            return false;
        }
    }

    @Override
    public void closeOrder(PaymentOrder order) {
        if (!isConfigured()) {
            return;
        }

        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getOrderNo());
            request.setBizContent(bizContent.toJSONString());

            AlipayTradeCloseResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("支付宝订单已关闭: orderNo={}", order.getOrderNo());
            }
        } catch (AlipayApiException e) {
            log.error("关闭支付宝订单失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    @Override
    public boolean isConfigured() {
        return alipayClient != null && alipayConfig.isConfigured();
    }

    /**
     * 解析表单参数
     */
    private Map<String, String> parseFormParams(String body) {
        Map<String, String> params = new HashMap<>();
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = java.net.URLDecoder.decode(pair.substring(0, idx), java.nio.charset.StandardCharsets.UTF_8);
                    String value = java.net.URLDecoder.decode(pair.substring(idx + 1), java.nio.charset.StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }
}
