package cn.sdh.travel.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付订单实体类
 */
@Data
@TableName("payment_order")
public class PaymentOrder {

    /**
     * 订单状态枚举
     */
    public static final int STATUS_PENDING = 0;    // 待支付
    public static final int STATUS_PAID = 1;       // 已支付
    public static final int STATUS_CANCELLED = 2;  // 已取消
    public static final int STATUS_REFUNDED = 3;   // 已退款

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 系统订单号（唯一）
     */
    private String orderNo;

    /**
     * 第三方支付订单号（微信/支付宝订单号）
     */
    private String thirdOrderNo;

    /**
     * 支付方式：WECHAT/ALIPAY
     */
    private String paymentMethod;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购买的会员等级：PRO/ENTERPRISE
     */
    private String memberLevel;

    /**
     * 购买月数
     */
    private Integer months;

    /**
     * 支付金额（分）
     */
    private Integer amount;

    /**
     * 状态：0-待支付，1-已支付，2-已取消，3-已退款
     */
    private Integer status;

    /**
     * 支付二维码链接
     */
    private String qrCodeUrl;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
