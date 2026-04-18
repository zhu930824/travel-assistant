package cn.sdh.travel.controller;

import cn.sdh.travel.common.result.Result;
import cn.sdh.travel.entity.dto.request.SmsLoginRequest;
import cn.sdh.travel.entity.dto.response.LoginResponse;
import cn.sdh.travel.service.SmsService;
import cn.sdh.travel.service.UserService;
import cn.sdh.travel.service.WechatAuthService;
import cn.sdh.travel.service.impl.WechatAuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理短信验证码登录和微信登录
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SmsService smsService;
    private final UserService userService;
    private final WechatAuthService wechatAuthService;
    private final WechatAuthServiceImpl wechatAuthServiceImpl;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public Result<Map<String, Object>> sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }

        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确");
        }

        try {
            String code = smsService.sendVerificationCode(phone);

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("message", "验证码发送成功");

            // 模拟模式下返回验证码，方便测试
            if (code != null) {
                data.put("mockCode", code);
            }

            return Result.success("验证码已发送", data);
        } catch (Exception e) {
            log.error("发送验证码失败: phone={}", phone, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手机号验证码登录/注册
     */
    @PostMapping("/sms/login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        log.info("手机号验证码登录请求: phone={}", request.getPhone());

        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
            return Result.error("验证码错误或已过期");
        }

        try {
            LoginResponse response = userService.loginByPhone(request.getPhone());
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("手机号登录失败: phone={}", request.getPhone(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取微信登录二维码URL
     */
    @GetMapping("/wechat/qrcode")
    public Result<Map<String, Object>> getWechatQrcode() {
        try {
            WechatAuthService.WechatLoginInfo loginInfo = wechatAuthService.getWechatLoginUrl();

            Map<String, Object> data = new HashMap<>();
            data.put("url", loginInfo.url());
            data.put("state", loginInfo.state());

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取微信登录URL失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 微信登录回调处理
     */
    @GetMapping("/wechat/callback")
    public void wechatCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        // 这个接口通常由微信服务器回调，返回重定向到前端页面
        // 实际项目中应该重定向到前端带token的页面
        log.info("微信登录回调: code={}, state={}", code, state);
    }

    /**
     * 轮询检查微信登录状态
     */
    @GetMapping("/wechat/check")
    public Result<Map<String, Object>> checkWechatLogin(@RequestParam String state) {
        try {
            LoginResponse response = wechatAuthServiceImpl.checkLoginState(state);
            Map<String, Object> data = new HashMap<>();

            if (response == null) {
                data.put("loggedIn", false);
                return Result.success(data);
            }

            data.put("loggedIn", true);
            data.put("token", response.getToken());
            data.put("userId", response.getUserId());
            data.put("username", response.getUsername());
            data.put("nickname", response.getNickname());
            data.put("avatar", response.getAvatar());
            data.put("memberLevel", response.getMemberLevel());
            data.put("memberLevelName", response.getMemberLevelName());

            return Result.success(data);
        } catch (Exception e) {
            log.error("检查微信登录状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 微信登录（通过前端传code）
     */
    @PostMapping("/wechat/login")
    public Result<LoginResponse> wechatLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        String state = params.get("state");

        if (code == null || code.trim().isEmpty()) {
            return Result.error("授权码不能为空");
        }

        try {
            LoginResponse response = wechatAuthService.handleCallback(code, state);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error(e.getMessage());
        }
    }
}
