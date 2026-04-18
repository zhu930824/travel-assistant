package cn.sdh.travel.config;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 微信支付配置类
 * 只有在配置了完整的微信支付信息时才会启用
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.pay")
public class WxPayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * API V3密钥
     */
    private String apiV3Key;

    /**
     * 商户私钥路径
     */
    private String privateKeyPath;

    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 支付回调地址
     */
    private String notifyUrl;

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        return isNotEmpty(appId) && !appId.equals("your_app_id_here")
                && isNotEmpty(mchId) && !mchId.equals("your_mch_id_here")
                && isNotEmpty(apiV3Key) && !apiV3Key.equals("your_api_v3_key_here")
                && isNotEmpty(merchantSerialNumber) && !merchantSerialNumber.equals("your_serial_number_here");
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 获取商户私钥内容
     */
    public String getPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource("apiclient_key.pem");
            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String key = reader.lines().collect(Collectors.joining("\n"));
                // 检查是否是示例文件
                if (key.contains("示例私钥") || key.contains("这是一个示例")) {
                    log.warn("使用的是示例私钥文件，请替换为真实的商户私钥");
                    return null;
                }
                return key;
            }
        } catch (IOException e) {
            log.warn("无法读取商户私钥文件: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 微信支付RSA配置
     * 只有在配置完整时才创建Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true")
    public RSAAutoCertificateConfig rsaAutoCertificateConfig() {
        if (!isConfigured()) {
            log.warn("微信支付配置不完整，跳过初始化");
            return null;
        }

        String privateKey = getPrivateKey();
        if (privateKey == null) {
            log.warn("商户私钥无效，跳过微信支付初始化");
            return null;
        }

        try {
            return new RSAAutoCertificateConfig.Builder()
                    .merchantId(mchId)
                    .apiV3Key(apiV3Key)
                    .merchantSerialNumber(merchantSerialNumber)
                    .privateKey(privateKey)
                    .build();
        } catch (Exception e) {
            log.error("初始化微信支付配置失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Native支付服务
     * 只有在配置完整时才创建Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true")
    public NativePayService nativePayService(RSAAutoCertificateConfig config) {
        if (config == null) {
            log.warn("微信支付配置不可用，NativePayService未初始化");
            return null;
        }
        return new NativePayService.Builder().config(config).build();
    }
}
