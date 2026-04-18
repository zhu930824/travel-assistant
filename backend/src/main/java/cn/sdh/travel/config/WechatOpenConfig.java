package cn.sdh.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信开放平台登录配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.open-platform")
public class WechatOpenConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 授权回调地址
     */
    private String redirectUri;

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        return isNotEmpty(appId) && !appId.equals("your_app_id")
                && isNotEmpty(appSecret) && !appSecret.equals("your_app_secret");
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
