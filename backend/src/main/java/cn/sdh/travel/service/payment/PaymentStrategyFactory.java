package cn.sdh.travel.service.payment;

import cn.sdh.travel.common.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 支付策略工厂
 * 根据支付方式获取对应的策略实现
 */
@Slf4j
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategyMap;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getMethod,
                        Function.identity()
                ));
        log.info("已加载 {} 个支付策略: {}",
                strategyMap.size(),
                strategyMap.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    /**
     * 获取支付策略
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + method);
        }
        return strategy;
    }

    /**
     * 获取已配置可用的支付方式列表
     */
    public List<PaymentMethod> getAvailableMethods() {
        return strategyMap.entrySet().stream()
                .filter(e -> e.getValue().isConfigured())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 检查指定支付方式是否可用
     */
    public boolean isAvailable(PaymentMethod method) {
        PaymentStrategy strategy = strategyMap.get(method);
        return strategy != null && strategy.isConfigured();
    }
}
