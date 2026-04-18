package cn.sdh.travel.controller;

import cn.sdh.travel.common.context.UserContext;
import cn.sdh.travel.common.enums.MemberLevel;
import cn.sdh.travel.common.result.Result;
import cn.sdh.travel.entity.domain.User;
import cn.sdh.travel.entity.dto.request.LoginRequest;
import cn.sdh.travel.entity.dto.request.RegisterRequest;
import cn.sdh.travel.entity.dto.request.UpgradeRequest;
import cn.sdh.travel.entity.dto.response.LoginResponse;
import cn.sdh.travel.entity.dto.response.PlanRecordResponse;
import cn.sdh.travel.service.PlanRecordService;
import cn.sdh.travel.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PlanRecordService planRecordService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: username={}", request.getUsername());
        User user = userService.register(request);
        // 清除密码，不返回给前端
        user.setPassword(null);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 获取当前用户信息（包含会员信息）
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        User user = userService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        MemberLevel memberLevel = MemberLevel.fromCode(user.getMemberLevel());
        int usedPlans = planRecordService.getMonthlyCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("memberLevel", memberLevel.getCode());
        data.put("memberLevelName", memberLevel.getName());
        data.put("memberExpireTime", user.getMemberExpireTime());
        data.put("usedPlans", usedPlans);
        data.put("planLimit", memberLevel.getMonthlyLimit());
        data.put("canPlan", userService.canPlan(userId));

        return Result.success(data);
    }

    /**
     * 检查是否可以规划
     */
    @GetMapping("/can-plan")
    public Result<Map<String, Object>> checkCanPlan() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        boolean canPlan = userService.canPlan(userId);
        int usedPlans = planRecordService.getMonthlyCount(userId);
        MemberLevel memberLevel = MemberLevel.FREE;

        User user = userService.findById(userId);
        if (user != null) {
            memberLevel = MemberLevel.fromCode(user.getMemberLevel());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("canPlan", canPlan);
        data.put("usedPlans", usedPlans);
        data.put("planLimit", memberLevel.getMonthlyLimit());
        data.put("memberLevel", memberLevel.getCode());

        return Result.success(data);
    }

    /**
     * 获取用户规划记录列表
     */
    @GetMapping("/plan-records")
    public Result<List<PlanRecordResponse>> getPlanRecords(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        List<PlanRecordResponse> records = planRecordService.getUserRecords(userId, Math.min(limit, 50));
        return Result.success(records);
    }

    /**
     * 升级会员
     */
    @PostMapping("/upgrade")
    public Result<Map<String, Object>> upgradeMember(@Valid @RequestBody UpgradeRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }

        log.info("会员升级请求: userId={}, memberLevel={}, months={}", userId, request.getMemberLevel(), request.getMonths());

        // 验证会员等级
        MemberLevel memberLevel = MemberLevel.fromCode(request.getMemberLevel());
        if (memberLevel == MemberLevel.FREE) {
            return Result.error("不支持的会员等级");
        }

        // 更新会员等级
        userService.updateMemberLevel(userId, request.getMemberLevel(), request.getMonths());

        // 返回更新后的用户信息
        User user = userService.findById(userId);
        int usedPlans = planRecordService.getMonthlyCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("memberLevel", memberLevel.getCode());
        data.put("memberLevelName", memberLevel.getName());
        data.put("memberExpireTime", user.getMemberExpireTime());
        data.put("planLimit", memberLevel.getMonthlyLimit());
        data.put("usedPlans", usedPlans);

        return Result.success("升级成功", data);
    }
}