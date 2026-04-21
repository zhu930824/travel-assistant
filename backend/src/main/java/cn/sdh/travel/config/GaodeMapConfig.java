package cn.sdh.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图API配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gaode.map")
public class GaodeMapConfig {

    /**
     * 高德地图API密钥
     */
    private String apiKey = "6853e704612bd320c0b452a5be5c1efc";

    /**
     * API基础URL
     */
    private String baseUrl = "https://restapi.amap.com/v3";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;

    /**
     * 是否启用
     */
    private boolean enabled = true;
}
