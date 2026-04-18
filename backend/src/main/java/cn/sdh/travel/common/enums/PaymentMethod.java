package cn.sdh.travel.common.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
public enum PaymentMethod {

    WECHAT("WECHAT", "微信支付"),
    ALIPAY("ALIPAY", "支付宝");

    private final String code;
    private final String name;

    PaymentMethod(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PaymentMethod fromCode(String code) {
        if (code == null) {
            return WECHAT;
        }
        for (PaymentMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return WECHAT;
    }
}
