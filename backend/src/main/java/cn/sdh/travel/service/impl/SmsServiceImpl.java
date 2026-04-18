package cn.sdh.travel.service.impl;

import cn.sdh.travel.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务实现（模拟模式）
 * 验证码存储在内存中，后续可对接阿里云/腾讯云短信服务
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${sms.code-expire-minutes:5}")
    private int codeExpireMinutes;

    /**
     * 验证码存储：key=手机号, value=验证码信息
     */
    private final ConcurrentHashMap<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    /**
     * 发送间隔限制：key=手机号, value=上次发送时间
     */
    private final ConcurrentHashMap<String, LocalDateTime> sendInterval = new ConcurrentHashMap<>();

    private static final int SEND_INTERVAL_SECONDS = 60;

    @Override
    public String sendVerificationCode(String phone) {
        // 检查发送间隔
        LocalDateTime lastSendTime = sendInterval.get(phone);
        if (lastSendTime != null && lastSendTime.plusSeconds(SEND_INTERVAL_SECONDS).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("发送过于频繁，请稍后再试");
        }

        // 生成6位随机验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 存储验证码
        codeStore.put(phone, new CodeEntry(code, LocalDateTime.now().plusMinutes(codeExpireMinutes)));
        sendInterval.put(phone, LocalDateTime.now());

        if (mockEnabled) {
            // 模拟模式：日志输出并返回验证码
            log.info("【模拟短信】手机号: {}, 验证码: {}", phone, code);
            return code;
        } else {
            // 真实模式：调用短信服务商API
            // TODO: 对接阿里云/腾讯云短信服务
            log.info("发送短信验证码到手机: {}", phone);
            return null;
        }
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        CodeEntry entry = codeStore.get(phone);
        if (entry == null) {
            return false;
        }

        // 检查是否过期
        if (entry.expireTime.isBefore(LocalDateTime.now())) {
            codeStore.remove(phone);
            return false;
        }

        // 验证码正确则移除（一次性使用）
        if (entry.code.equals(code)) {
            codeStore.remove(phone);
            sendInterval.remove(phone);
            return true;
        }

        return false;
    }

    /**
     * 验证码存储条目
     */
    private record CodeEntry(String code, LocalDateTime expireTime) {
    }
}
