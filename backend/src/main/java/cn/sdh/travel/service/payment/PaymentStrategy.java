package cn.sdh.travel.service.payment;

import cn.sdh.travel.common.enums.PaymentMethod;
import cn.sdh.travel.entity.domain.PaymentOrder;

import java.util.Map;

/**
 * 支付策略接口
 * 定义支付的标准方法，不同支付方式各自实现
 */
public interface PaymentStrategy {

    /**
     * 获取此策略对应的支付方式
     */
    PaymentMethod getMethod();

    /**
     * 创建支付订单
     * @param order 支付订单（已包含基本信息，策略需填充qrCodeUrl等）
     */
    void createOrder(PaymentOrder order);

    /**
     * 处理支付回调
     * @param body 回调请求体
     * @param headers 回调请求头
     * @return 处理结果（包含orderNo和是否成功）
     */
    CallbackResult handleCallback(String body, Map<String, String> headers);

    /**
     * 主动查询第三方订单状态
     * @param order 支付订单
     * @return 是否支付成功
     */
    boolean queryPayStatus(PaymentOrder order);

    /**
     * 关闭订单
     * @param order 支付订单
     */
    void closeOrder(PaymentOrder order);

    /**
     * 当前策略是否已配置可用
     */
    boolean isConfigured();

    /**
     * 回调处理结果
     */
    record CallbackResult(boolean success, String orderNo, String thirdOrderNo, String message) {
        public static CallbackResult fail(String message) {
            return new CallbackResult(false, null, null, message);
        }

        public static CallbackResult success(String orderNo, String thirdOrderNo) {
            return new CallbackResult(true, orderNo, thirdOrderNo, "成功");
        }
    }
}
