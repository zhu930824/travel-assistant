package cn.sdh.travel.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import cn.sdh.travel.common.enums.MemberLevel;
import cn.sdh.travel.common.exception.BusinessException;
import cn.sdh.travel.common.utils.JwtUtil;
import cn.sdh.travel.entity.domain.User;
import cn.sdh.travel.entity.dto.request.LoginRequest;
import cn.sdh.travel.entity.dto.request.RegisterRequest;
import cn.sdh.travel.entity.dto.response.LoginResponse;
import cn.sdh.travel.mapper.PlanRecordMapper;
import cn.sdh.travel.mapper.UserMapper;
import cn.sdh.travel.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PlanRecordMapper planRecordMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existUser = findByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        // 使用BCrypt加密密码
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setMemberLevel(MemberLevel.FREE.getCode()); // 默认免费版
        user.setStatus(1); // 默认正常状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: username={}", user.getUsername());

        return user;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 获取会员等级
        MemberLevel memberLevel = MemberLevel.fromCode(user.getMemberLevel());

        // 获取本月已使用次数
        int usedPlans = getUsedPlansThisMonth(user.getId());

        log.info("用户登录成功: userId={}, username={}, memberLevel={}", user.getId(), user.getUsername(), memberLevel.getName());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .memberLevel(memberLevel.getCode())
                .memberLevelName(memberLevel.getName())
                .memberExpireTime(user.getMemberExpireTime())
                .usedPlans(usedPlans)
                .planLimit(memberLevel.getMonthlyLimit())
                .build();
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }

    @Override
    public User findByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    @Transactional
    public LoginResponse loginByPhone(String phone) {
        User user = findByPhone(phone);

        if (user == null) {
            // 自动注册
            user = new User();
            user.setPhone(phone);
            user.setUsername(phone);
            user.setPassword(BCrypt.hashpw(phone));
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setMemberLevel(MemberLevel.FREE.getCode());
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("手机号自动注册: phone={}", phone);
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getNickname());

        MemberLevel memberLevel = MemberLevel.fromCode(user.getMemberLevel());
        int usedPlans = getUsedPlansThisMonth(user.getId());

        log.info("手机号登录成功: userId={}, phone={}", user.getId(), phone);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .memberLevel(memberLevel.getCode())
                .memberLevelName(memberLevel.getName())
                .memberExpireTime(user.getMemberExpireTime())
                .usedPlans(usedPlans)
                .planLimit(memberLevel.getMonthlyLimit())
                .build();
    }

    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public int getUsedPlansThisMonth(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startTime = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        return planRecordMapper.countMonthlyPlans(userId, startTime, endTime);
    }

    @Override
    public boolean canPlan(Long userId) {
        User user = findById(userId);
        if (user == null) {
            return false;
        }

        MemberLevel memberLevel = MemberLevel.fromCode(user.getMemberLevel());

        // 企业版和专业版无限次数
        if (memberLevel == MemberLevel.PRO || memberLevel == MemberLevel.ENTERPRISE) {
            // 检查是否过期
            if (user.getMemberExpireTime() != null && user.getMemberExpireTime().isBefore(LocalDateTime.now())) {
                return false;
            }
            return true;
        }

        // 免费版检查次数
        int usedPlans = getUsedPlansThisMonth(userId);
        return usedPlans < memberLevel.getMonthlyLimit();
    }

    @Override
    @Transactional
    public void updateMemberLevel(Long userId, String memberLevel, int months) {
        User user = findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setMemberLevel(memberLevel);
        if (months > 0) {
            LocalDateTime expireTime = LocalDateTime.now().plusMonths(months);
            user.setMemberExpireTime(expireTime);
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        log.info("更新用户会员等级: userId={}, memberLevel={}, months={}", userId, memberLevel, months);
    }
}
