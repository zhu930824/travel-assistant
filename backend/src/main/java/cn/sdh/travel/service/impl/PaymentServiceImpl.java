package cn.sdh.travel.service.impl;

import cn.sdh.travel.common.enums.MemberLevel;
import cn.sdh.travel.common.enums.PaymentMethod;
import cn.sdh.travel.common.exception.BusinessException;
import cn.sdh.travel.entity.domain.PaymentOrder;
import cn.sdh.travel.mapper.PaymentOrderMapper;
import cn.sdh.travel.service.PaymentService;
import cn.sdh.travel.service.UserService;
import cn.sdh.travel.service.payment.PaymentStrategy;
import cn.sdh.travel.service.payment.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderMapper paymentOrderMapper;
    private final UserService userService;
    private final PaymentStrategyFactory strategyFactory;

    /**
     * PRO版价格（分/月）
     */
    private static final int PRO_PRICE_PER_MONTH = 2900; // ¥29

    /**
     * 订单过期时间（分钟）
     */
    private static final int ORDER_EXPIRE_MINUTES = 30;

    @Override
    @Transactional
    public PaymentOrder createOrder(Long userId, String memberLevel, int months) {
        return createOrder(userId, memberLevel, months, PaymentMethod.WECHAT.getCode());
    }

    @Override
    @Transactional
    public PaymentOrder createOrder(Long userId, String memberLevel, int months, String paymentMethod) {
        // 验证用户
        var user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证会员等级
        if (!MemberLevel.PRO.getCode().equals(memberLevel) && !MemberLevel.ENTERPRISE.getCode().equals(memberLevel)) {
            throw new BusinessException("不支持的会员等级");
        }

        // 计算金额
        int amount = calculateAmount(memberLevel, months);
        if (amount <= 0 && MemberLevel.ENTERPRISE.getCode().equals(memberLevel)) {
            throw new BusinessException("企业版请联系客服购买");
        }

        // 获取支付方式
        PaymentMethod method = PaymentMethod.fromCode(paymentMethod);

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建数据库订单记录
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMemberLevel(memberLevel);
        order.setMonths(months);
        order.setAmount(amount);
        order.setPaymentMethod(method.getCode());
        order.setStatus(PaymentOrder.STATUS_PENDING);
        order.setExpireTime(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 获取对应支付策略并创建订单
        PaymentStrategy strategy = strategyFactory.getStrategy(method);
        strategy.createOrder(order);

        // 保存订单
        paymentOrderMapper.insert(order);

        log.info("创建支付订单成功: orderNo={}, userId={}, memberLevel={}, months={}, amount={}, paymentMethod={}",
                orderNo, userId, memberLevel, months, amount, method.getName());

        return order;
    }

    @Override
    @Transactional
    public Map<String, String> handleCallback(String notification, Map<String, String> headers) {
        log.info("收到微信支付回调");
        return handleCallback(PaymentMethod.WECHAT, notification, headers);
    }

    @Override
    @Transactional
    public Map<String, String> handleCallback(PaymentMethod paymentMethod, String body, Map<String, String> headers) {
        log.info("收到支付回调: paymentMethod={}", paymentMethod.getName());

        PaymentStrategy strategy = strategyFactory.getStrategy(paymentMethod);
        PaymentStrategy.CallbackResult result = strategy.handleCallback(body, headers);

        if (!result.success()) {
            return Map.of("code", "FAIL", "message", result.message());
        }

        // 查询订单
        PaymentOrder order = paymentOrderMapper.findByOrderNo(result.orderNo());
        if (order == null) {
            log.error("订单不存在: orderNo={}", result.orderNo());
            return Map.of("code", "FAIL", "message", "订单不存在");
        }

        // 检查订单状态
        if (order.getStatus() != PaymentOrder.STATUS_PENDING) {
            log.info("订单已处理: orderNo={}, status={}", result.orderNo(), order.getStatus());
            return Map.of("code", "SUCCESS", "message", "成功");
        }

        // 更新订单状态
        order.setStatus(PaymentOrder.STATUS_PAID);
        order.setThirdOrderNo(result.thirdOrderNo());
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        paymentOrderMapper.updateById(order);

        // 更新用户会员等级
        userService.updateMemberLevel(order.getUserId(), order.getMemberLevel(), order.getMonths());

        log.info("支付成功，订单处理完成: orderNo={}, userId={}, thirdOrderNo={}",
                result.orderNo(), order.getUserId(), result.thirdOrderNo());

        return Map.of("code", "SUCCESS", "message", "成功");
    }

    @Override
    public PaymentOrder queryOrder(String orderNo) {
        PaymentOrder order = paymentOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 如果订单是待支付状态，主动查询支付状态
        if (order.getStatus() == PaymentOrder.STATUS_PENDING) {
            PaymentMethod method = PaymentMethod.fromCode(order.getPaymentMethod());
            PaymentStrategy strategy = strategyFactory.getStrategy(method);

            boolean paid = strategy.queryPayStatus(order);
            if (paid) {
                // 更新订单状态
                order.setStatus(PaymentOrder.STATUS_PAID);
                order.setPayTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                paymentOrderMapper.updateById(order);

                // 更新用户会员等级
                userService.updateMemberLevel(order.getUserId(), order.getMemberLevel(), order.getMonths());
            }
        }

        return order;
    }

    @Override
    public List<PaymentOrder> getUserOrders(Long userId) {
        return paymentOrderMapper.findByUserId(userId, 20);
    }

    @Override
    @Transactional
    public void closeExpiredOrders() {
        List<PaymentOrder> expiredOrders = paymentOrderMapper.findExpiredOrders();
        for (PaymentOrder order : expiredOrders) {
            PaymentMethod method = PaymentMethod.fromCode(order.getPaymentMethod());
            PaymentStrategy strategy = strategyFactory.getStrategy(method);

            strategy.closeOrder(order);

            // 更新订单状态
            order.setStatus(PaymentOrder.STATUS_CANCELLED);
            order.setUpdateTime(LocalDateTime.now());
            paymentOrderMapper.updateById(order);

            log.info("关闭过期订单: orderNo={}", order.getOrderNo());
        }
    }

    /**
     * 获取可用的支付方式列表
     */
    public List<PaymentMethod> getAvailableMethods() {
        return strategyFactory.getAvailableMethods();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "PAY" + timestamp + random;
    }

    /**
     * 计算金额（分）
     */
    private int calculateAmount(String memberLevel, int months) {
        if (MemberLevel.PRO.getCode().equals(memberLevel)) {
            return PRO_PRICE_PER_MONTH * months;
        }
        return 0;
    }
}
