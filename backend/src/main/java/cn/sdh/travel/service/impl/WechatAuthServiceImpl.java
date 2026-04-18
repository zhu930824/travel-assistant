package cn.sdh.travel.service.impl;

import cn.sdh.travel.common.enums.MemberLevel;
import cn.sdh.travel.common.exception.BusinessException;
import cn.sdh.travel.common.utils.JwtUtil;
import cn.sdh.travel.config.WechatOpenConfig;
import cn.sdh.travel.entity.domain.User;
import cn.sdh.travel.entity.dto.response.LoginResponse;
import cn.sdh.travel.mapper.UserMapper;
import cn.sdh.travel.service.WechatAuthService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信登录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthServiceImpl implements WechatAuthService {

    private final WechatOpenConfig wechatOpenConfig;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 存储state与登录状态的映射，用于轮询检查
     */
    private final ConcurrentHashMap<String, WechatLoginState> loginStateMap = new ConcurrentHashMap<>();

    /**
     * state有效期：10分钟
     */
    private static final long STATE_EXPIRE_MS = 10 * 60 * 1000;

    @Override
    public WechatLoginInfo getWechatLoginUrl() {
        if (!wechatOpenConfig.isConfigured()) {
            // 未配置微信开放平台，返回模拟URL
            String mockState = UUID.randomUUID().toString().replace("-", "");
            String mockUrl = "https://open.weixin.qq.com/connect/qrconnect?appid=mock"
                    + "&redirect_uri=" + URLEncoder.encode("https://your-domain.com/wechat-callback", StandardCharsets.UTF_8)
                    + "&response_type=code&scope=snsapi_login&state=" + mockState;

            // 存储state用于后续轮询
            loginStateMap.put(mockState, new WechatLoginState(false, null, System.currentTimeMillis()));

            log.info("微信登录未配置，返回模拟授权URL, state={}", mockState);
            return new WechatLoginInfo(mockUrl, mockState);
        }

        String state = UUID.randomUUID().toString().replace("-", "");
        String redirectUri = URLEncoder.encode(wechatOpenConfig.getRedirectUri(), StandardCharsets.UTF_8);
        String url = "https://open.weixin.qq.com/connect/qrconnect"
                + "?appid=" + wechatOpenConfig.getAppId()
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=snsapi_login"
                + "&state=" + state;

        loginStateMap.put(state, new WechatLoginState(false, null, System.currentTimeMillis()));

        log.info("生成微信登录URL, state={}", state);
        return new WechatLoginInfo(url, state);
    }

    @Override
    @Transactional
    public LoginResponse handleCallback(String code, String state) {
        // 清理过期的state
        cleanupExpiredStates();

        String openid;
        String unionid = null;
        String nickname = null;
        String avatar = null;

        if (wechatOpenConfig.isConfigured()) {
            // 真实模式：通过code换取access_token和用户信息
            JSONObject tokenResult = requestAccessToken(code);
            openid = tokenResult.getString("openid");
            unionid = tokenResult.getString("unionid");

            // 获取用户信息
            JSONObject userInfo = requestUserInfo(tokenResult.getString("access_token"), openid);
            if (userInfo != null) {
                nickname = userInfo.getString("nickname");
                avatar = userInfo.getString("headimgurl");
                if (unionid == null) {
                    unionid = userInfo.getString("unionid");
                }
            }
        } else {
            // 模拟模式：用code模拟openid
            openid = "mock_openid_" + code;
            nickname = "微信用户";
            log.info("微信登录模拟模式, code={}, mockOpenid={}", code, openid);
        }

        // 查找或创建用户
        User user = findByWechatOpenid(openid);
        if (user == null && unionid != null) {
            // 通过unionid查找（同一用户可能绑定多个应用）
            user = findByWechatUnionid(unionid);
        }

        if (user == null) {
            // 自动注册
            user = registerByWechat(openid, unionid, nickname, avatar);
            log.info("微信登录自动注册: openid={}", openid);
        } else {
            // 更新用户信息
            if (nickname != null && (user.getNickname() == null || user.getNickname().isEmpty())) {
                user.setNickname(nickname);
            }
            if (avatar != null && (user.getAvatar() == null || user.getAvatar().isEmpty())) {
                user.setAvatar(avatar);
            }
            if (unionid != null && user.getWechatUnionid() == null) {
                user.setWechatUnionid(unionid);
            }
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        // 更新登录状态
        if (state != null) {
            loginStateMap.put(state, new WechatLoginState(true, user.getId(), System.currentTimeMillis()));
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getNickname() != null ? user.getNickname() : "wx_user");

        MemberLevel memberLevel = MemberLevel.fromCode(user.getMemberLevel());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getPhone() != null ? user.getPhone() : "wx_" + user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .memberLevel(memberLevel.getCode())
                .memberLevelName(memberLevel.getName())
                .memberExpireTime(user.getMemberExpireTime())
                .usedPlans(0)
                .planLimit(memberLevel.getMonthlyLimit())
                .build();
    }

    /**
     * 检查微信登录状态（轮询接口）
     */
    public LoginResponse checkLoginState(String state) {
        cleanupExpiredStates();
        WechatLoginState loginState = loginStateMap.get(state);
        if (loginState == null) {
            throw new BusinessException("登录状态已过期，请重新获取二维码");
        }
        if (!loginState.loggedIn()) {
            return null; // 未登录
        }

        // 已登录，清理state并返回
        loginStateMap.remove(state);
        User user = userMapper.selectById(loginState.userId());

        String token = jwtUtil.generateToken(user.getId(), user.getNickname() != null ? user.getNickname() : "wx_user");
        MemberLevel memberLevel = MemberLevel.fromCode(user.getMemberLevel());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getPhone() != null ? user.getPhone() : "wx_" + user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .memberLevel(memberLevel.getCode())
                .memberLevelName(memberLevel.getName())
                .memberExpireTime(user.getMemberExpireTime())
                .usedPlans(0)
                .planLimit(memberLevel.getMonthlyLimit())
                .build();
    }

    private User findByWechatOpenid(String openid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getWechatOpenid, openid)
        );
    }

    private User findByWechatUnionid(String unionid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getWechatUnionid, unionid)
        );
    }

    @Transactional
    private User registerByWechat(String openid, String unionid, String nickname, String avatar) {
        User user = new User();
        user.setWechatOpenid(openid);
        user.setWechatUnionid(unionid);
        user.setNickname(nickname != null ? nickname : "微信用户");
        user.setAvatar(avatar);
        user.setMemberLevel(MemberLevel.FREE.getCode());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    /**
     * 通过code请求access_token
     */
    private JSONObject requestAccessToken(String code) {
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
                    + "?appid=" + wechatOpenConfig.getAppId()
                    + "&secret=" + wechatOpenConfig.getAppSecret()
                    + "&code=" + code
                    + "&grant_type=authorization_code";

            String result = cn.hutool.http.HttpUtil.get(url);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("获取微信access_token失败", e);
            throw new BusinessException("微信登录失败");
        }
    }

    /**
     * 通过access_token获取用户信息
     */
    private JSONObject requestUserInfo(String accessToken, String openid) {
        try {
            String url = "https://api.weixin.qq.com/sns/userinfo"
                    + "?access_token=" + accessToken
                    + "&openid=" + openid;

            String result = cn.hutool.http.HttpUtil.get(url);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("获取微信用户信息失败", e);
            return null;
        }
    }

    /**
     * 清理过期的登录状态
     */
    private void cleanupExpiredStates() {
        long now = System.currentTimeMillis();
        loginStateMap.entrySet().removeIf(entry ->
                now - entry.getValue().createTime() > STATE_EXPIRE_MS
        );
    }

    /**
     * 微信登录状态
     */
    private record WechatLoginState(boolean loggedIn, Long userId, long createTime) {
    }
}
