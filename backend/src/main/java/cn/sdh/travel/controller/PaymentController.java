package cn.sdh.travel.controller;

import cn.sdh.travel.common.context.UserContext;
import cn.sdh.travel.common.enums.PaymentMethod;
import cn.sdh.travel.common.result.Result;
import cn.sdh.travel.entity.domain.PaymentOrder;
import cn.sdh.travel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 获取可用的支付方式列表
     */
    @GetMapping("/methods")
    public Result<List<Map<String, String>>> getPaymentMethods() {
        List<PaymentMethod> methods = paymentService.getAvailableMethods();

        List<Map<String, String>> result = methods.stream()
                .map(m -> Map.of(
                        "code", m.getCode(),
                        "name", m.getName()
                ))
                .toList();

        return Result.success(result);
    }

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        String memberLevel = (String) params.get("memberLevel");
        Integer months = params.get("months") != null ? ((Number) params.get("months")).intValue() : 1;
        String paymentMethod = (String) params.getOrDefault("paymentMethod", PaymentMethod.WECHAT.getCode());

        log.info("创建支付订单请求: userId={}, memberLevel={}, months={}, paymentMethod={}", userId, memberLevel, months, paymentMethod);

        try {
            PaymentOrder order = paymentService.createOrder(userId, memberLevel, months, paymentMethod);

            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", order.getOrderNo());
            data.put("qrCodeUrl", order.getQrCodeUrl());
            data.put("amount", order.getAmount());
            data.put("memberLevel", order.getMemberLevel());
            data.put("months", order.getMonths());
            data.put("paymentMethod", order.getPaymentMethod());
            data.put("expireTime", order.getExpireTime());

            return Result.success("订单创建成功", data);
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付回调
     */
    @PostMapping("/callback/wechat")
    public Map<String, String> handleWechatCallback(
            @RequestBody String notification,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Wechatpay-Serial", serial);
        headers.put("Wechatpay-Nonce", nonce);
        headers.put("Wechatpay-Timestamp", timestamp);
        headers.put("Wechatpay-Signature", signature);

        log.info("收到微信支付回调");
        return paymentService.handleCallback(PaymentMethod.WECHAT, notification, headers);
    }

    /**
     * 支付宝支付回调
     */
    @PostMapping("/callback/alipay")
    public String handleAlipayCallback(@RequestBody String body) {
        log.info("收到支付宝支付回调");

        Map<String, String> result = paymentService.handleCallback(PaymentMethod.ALIPAY, body, new HashMap<>());

        // 支付宝要求返回 success 或 failure
        return "SUCCESS".equals(result.get("code")) ? "success" : "failure";
    }

    /**
     * 兼容旧版微信回调路径
     */
    @PostMapping("/callback")
    public Map<String, String> handleCallback(
            @RequestBody String notification,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Wechatpay-Serial", serial);
        headers.put("Wechatpay-Nonce", nonce);
        headers.put("Wechatpay-Timestamp", timestamp);
        headers.put("Wechatpay-Signature", signature);

        log.info("收到微信支付回调（旧版路径）");
        return paymentService.handleCallback(PaymentMethod.WECHAT, notification, headers);
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/status/{orderNo}")
    public Result<Map<String, Object>> queryOrder(@PathVariable String orderNo) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        try {
            PaymentOrder order = paymentService.queryOrder(orderNo);

            // 验证订单所属用户
            if (!order.getUserId().equals(userId)) {
                return Result.forbidden("无权查看此订单");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", order.getOrderNo());
            data.put("status", order.getStatus());
            data.put("statusText", getStatusText(order.getStatus()));
            data.put("amount", order.getAmount());
            data.put("payTime", order.getPayTime());

            return Result.success(data);
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户支付订单列表
     */
    @GetMapping("/orders")
    public Result<List<PaymentOrder>> getUserOrders() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        List<PaymentOrder> orders = paymentService.getUserOrders(userId);
        return Result.success(orders);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(int status) {
        return switch (status) {
            case PaymentOrder.STATUS_PENDING -> "待支付";
            case PaymentOrder.STATUS_PAID -> "已支付";
            case PaymentOrder.STATUS_CANCELLED -> "已取消";
            case PaymentOrder.STATUS_REFUNDED -> "已退款";
            default -> "未知";
        };
    }
}
