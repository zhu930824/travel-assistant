package cn.sdh.travel.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付配置类
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 同步跳转地址
     */
    private String returnUrl;

    /**
     * 网关地址（沙箱环境和生产环境不同）
     */
    private String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /**
     * 签名方式
     */
    private String signType = "RSA2";

    /**
     * 字符编码
     */
    private String charset = "UTF-8";

    /**
     * 是否沙箱环境
     */
    private boolean sandbox = true;

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        return isNotEmpty(appId) && !appId.equals("your_alipay_app_id")
                && isNotEmpty(privateKey) && !privateKey.equals("your_private_key_here")
                && isNotEmpty(alipayPublicKey);
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 创建支付宝客户端
     */
    @Bean
    public AlipayClient alipayClient() {
        if (!isConfigured()) {
            log.warn("支付宝配置不完整，AlipayClient未初始化");
            return null;
        }

        String url = sandbox ? "https://openapi-sandbox.dl.alipaydev.com/gateway.do" : serverUrl;

        return new DefaultAlipayClient(
                url,
                appId,
                privateKey,
                "json",
                charset,
                alipayPublicKey,
                signType
        );
    }
}
