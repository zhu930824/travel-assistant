package cn.sdh.travel.service;

/**
 * 短信验证码服务
 */
public interface SmsService {

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 模拟模式下返回验证码，真实模式下返回null
     */
    String sendVerificationCode(String phone);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String phone, String code);
}
