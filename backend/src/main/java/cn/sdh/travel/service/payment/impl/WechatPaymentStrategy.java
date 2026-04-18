package cn.sdh.travel.service.payment.impl;

import cn.sdh.travel.common.enums.PaymentMethod;
import cn.sdh.travel.config.WxPayConfig;
import cn.sdh.travel.entity.domain.PaymentOrder;
import cn.sdh.travel.service.payment.PaymentStrategy;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信支付策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatPaymentStrategy implements PaymentStrategy {

    private final WxPayConfig wxPayConfig;

    @Autowired(required = false)
    private NativePayService nativePayService;

    @Autowired(required = false)
    private RSAAutoCertificateConfig rsaAutoCertificateConfig;

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.WECHAT;
    }

    @Override
    public void createOrder(PaymentOrder order) {
        if (!isConfigured()) {
            log.warn("微信支付未配置，创建模拟支付链接");
            String mockQrCodeUrl = "weixin://wxpay/bizpayurl?pr=mock_" + order.getOrderNo();
            order.setQrCodeUrl(mockQrCodeUrl);
            return;
        }

        try {
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wxPayConfig.getAppId());
            request.setMchid(wxPayConfig.getMchId());
            request.setDescription(order.getMemberLevel().equals("PRO") ? "TravelAI专业版会员" : "TravelAI企业版会员");
            request.setOutTradeNo(order.getOrderNo());
            request.setNotifyUrl(wxPayConfig.getNotifyUrl());

            Amount amountObj = new Amount();
            amountObj.setTotal(order.getAmount());
            amountObj.setCurrency("CNY");
            request.setAmount(amountObj);

            PrepayResponse response = nativePayService.prepay(request);
            order.setQrCodeUrl(response.getCodeUrl());

            log.info("微信支付订单创建成功: orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("创建微信支付订单失败: orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("创建微信支付订单失败: " + e.getMessage());
        }
    }

    @Override
    public CallbackResult handleCallback(String body, Map<String, String> headers) {
        if (rsaAutoCertificateConfig == null) {
            log.warn("微信支付未配置，无法处理回调");
            return CallbackResult.fail("微信支付未配置");
        }

        try {
            NotificationParser parser = new NotificationParser(rsaAutoCertificateConfig);

            // 构建请求参数
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(headers.get("Wechatpay-Serial"))
                    .nonce(headers.get("Wechatpay-Nonce"))
                    .signature(headers.get("Wechatpay-Signature"))
                    .timestamp(headers.get("Wechatpay-Timestamp"))
                    .body(body)
                    .build();

            Transaction transaction = parser.parse(requestParam, Transaction.class);

            if (transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                return CallbackResult.success(transaction.getOutTradeNo(), transaction.getTransactionId());
            }

            return CallbackResult.fail("支付未完成");
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return CallbackResult.fail("处理失败: " + e.getMessage());
        }
    }

    @Override
    public boolean queryPayStatus(PaymentOrder order) {
        if (!isConfigured()) {
            return false;
        }

        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(wxPayConfig.getMchId());
            request.setOutTradeNo(order.getOrderNo());

            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
            return transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS;
        } catch (Exception e) {
            log.warn("查询微信支付订单状态失败: orderNo={}", order.getOrderNo(), e);
            return false;
        }
    }

    @Override
    public void closeOrder(PaymentOrder order) {
        if (!isConfigured()) {
            return;
        }

        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(wxPayConfig.getMchId());
            request.setOutTradeNo(order.getOrderNo());
            nativePayService.closeOrder(request);
            log.info("微信支付订单已关闭: orderNo={}", order.getOrderNo());
        } catch (Exception e) {
            log.error("关闭微信支付订单失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    @Override
    public boolean isConfigured() {
        return nativePayService != null && wxPayConfig.isConfigured();
    }
}
