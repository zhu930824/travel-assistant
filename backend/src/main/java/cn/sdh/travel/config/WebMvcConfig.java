package cn.sdh.travel.config;

import cn.sdh.travel.common.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 配置SSE异步请求超时时间
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // SSE连接超时时间设置为5分钟
        configurer.setDefaultTimeout(300000);
    }



    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册JWT拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        // 登录注册相关
                        "/api/user/login",
                        "/api/user/register",
                        // 认证相关（短信验证码 + 微信登录）
                        "/api/auth/sms/send",
                        "/api/auth/sms/login",
                        "/api/auth/wechat/qrcode",
                        "/api/auth/wechat/callback",
                        "/api/auth/wechat/check",
                        "/api/auth/wechat/login",
                        // 支付回调（微信/支付宝服务器调用，无需登录）
                        "/api/payment/callback",
                        "/api/payment/callback/wechat",
                        "/api/payment/callback/alipay",
                        // 静态资源
                        "/static/**",
                        "/upload/**",
                        // 攻略公开接口
                        "/api/guide/recommend",
                        "/api/guide/{id}",
                        // Swagger相关
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        // 错误页面
                        "/error",
                        // 健康检查
                        "/actuator/**"
                );
    }
}
