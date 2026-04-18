-- 创建支付订单表
CREATE TABLE IF NOT EXISTS `payment_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '系统订单号（唯一）',
    `wx_order_no` VARCHAR(64) DEFAULT NULL COMMENT '微信支付订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `member_level` VARCHAR(20) NOT NULL COMMENT '购买的会员等级：PRO/ENTERPRISE',
    `months` INT NOT NULL DEFAULT 1 COMMENT '购买月数',
    `amount` INT NOT NULL COMMENT '支付金额（分）',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-已取消，3-已退款',
    `qr_code_url` VARCHAR(500) DEFAULT NULL COMMENT '支付二维码链接',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '订单过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';