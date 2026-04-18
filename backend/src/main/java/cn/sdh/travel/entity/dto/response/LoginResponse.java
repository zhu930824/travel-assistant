package cn.sdh.travel.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 会员等级
     */
    private String memberLevel;

    /**
     * 会员等级名称
     */
    private String memberLevelName;

    /**
     * 会员过期时间
     */
    private LocalDateTime memberExpireTime;

    /**
     * 本月已使用规划次数
     */
    private Integer usedPlans;

    /**
     * 本月规划次数限制
     */
    private Integer planLimit;
}
