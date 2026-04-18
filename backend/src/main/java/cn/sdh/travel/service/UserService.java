package cn.sdh.travel.service;

import cn.sdh.travel.entity.dto.request.LoginRequest;
import cn.sdh.travel.entity.dto.request.RegisterRequest;
import cn.sdh.travel.entity.dto.response.LoginResponse;
import cn.sdh.travel.entity.domain.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册成功后的用户信息
     */
    User register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应，包含token和用户信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(String username);

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户实体
     */
    User findById(Long userId);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户实体
     */
    User findByPhone(String phone);

    /**
     * 手机号验证码登录/注册（自动注册）
     * @param phone 手机号
     * @return 登录响应
     */
    LoginResponse loginByPhone(String phone);

    /**
     * 获取用户本月已使用规划次数
     * @param userId 用户ID
     * @return 已使用次数
     */
    int getUsedPlansThisMonth(Long userId);

    /**
     * 检查用户是否可以继续规划
     * @param userId 用户ID
     * @return true-可以规划，false-已达上限
     */
    boolean canPlan(Long userId);

    /**
     * 更新用户会员等级
     * @param userId 用户ID
     * @param memberLevel 会员等级
     * @param months 月数
     */
    void updateMemberLevel(Long userId, String memberLevel, int months);
}