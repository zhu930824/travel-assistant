package cn.sdh.travel.service;

import cn.sdh.travel.common.enums.PaymentMethod;
import cn.sdh.travel.entity.domain.PaymentOrder;

import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付订单（默认微信支付）
     * @param userId 用户ID
     * @param memberLevel 会员等级
     * @param months 购买月数
     * @return 支付订单信息（包含二维码URL）
     */
    PaymentOrder createOrder(Long userId, String memberLevel, int months);

    /**
     * 创建支付订单（指定支付方式）
     * @param userId 用户ID
     * @param memberLevel 会员等级
     * @param months 购买月数
     * @param paymentMethod 支付方式
     * @return 支付订单信息（包含二维码URL）
     */
    PaymentOrder createOrder(Long userId, String memberLevel, int months, String paymentMethod);

    /**
     * 处理微信支付回调（兼容旧接口）
     * @param notification 回调通知内容
     * @param headers 回调请求头
     * @return 处理结果
     */
    Map<String, String> handleCallback(String notification, Map<String, String> headers);

    /**
     * 处理支付回调（指定支付方式）
     * @param paymentMethod 支付方式
     * @param body 回调请求体
     * @param headers 回调请求头
     * @return 处理结果
     */
    Map<String, String> handleCallback(PaymentMethod paymentMethod, String body, Map<String, String> headers);

    /**
     * 查询订单状态
     * @param orderNo 订单号
     * @return 订单信息
     */
    PaymentOrder queryOrder(String orderNo);

    /**
     * 获取用户支付订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<PaymentOrder> getUserOrders(Long userId);

    /**
     * 关闭过期订单
     */
    void closeExpiredOrders();

    /**
     * 获取可用的支付方式列表
     */
    List<PaymentMethod> getAvailableMethods();
}