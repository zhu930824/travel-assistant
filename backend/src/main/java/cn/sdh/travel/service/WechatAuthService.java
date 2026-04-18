package cn.sdh.travel.service;

import cn.sdh.travel.entity.dto.response.LoginResponse;
import cn.sdh.travel.entity.domain.User;

/**
 * 微信登录服务
 */
public interface WechatAuthService {

    /**
     * 获取微信登录授权URL
     * @return 授权URL和state参数
     */
    WechatLoginInfo getWechatLoginUrl();

    /**
     * 处理微信登录回调
     * @param code 授权码
     * @param state 状态参数
     * @return 登录响应
     */
    LoginResponse handleCallback(String code, String state);

    /**
     * 微信登录信息
     */
    record WechatLoginInfo(String url, String state) {}
}
