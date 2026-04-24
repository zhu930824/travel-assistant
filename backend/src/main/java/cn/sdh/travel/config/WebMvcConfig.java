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
        configurer.setDefaultTimeout(300000);
    }

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/auth/sms/send",
                        "/api/auth/sms/login",
                        "/api/auth/wechat/qrcode",
                        "/api/auth/wechat/callback",
                        "/api/auth/wechat/check",
                        "/api/auth/wechat/login",
                        "/api/payment/callback",
                        "/api/payment/callback/wechat",
                        "/api/payment/callback/alipay",
                        "/static/**",
                        "/upload/**",
                        "/api/guide/recommend",
                        "/api/guide/{id}",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/error",
                        "/actuator/**"
                );
    }
}
