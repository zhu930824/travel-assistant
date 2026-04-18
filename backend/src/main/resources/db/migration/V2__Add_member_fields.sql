-- 添加会员相关字段
ALTER TABLE `user` ADD COLUMN `member_level` VARCHAR(20) DEFAULT 'FREE' COMMENT '会员等级：FREE-免费版, PRO-专业版, ENTERPRISE-企业版' AFTER `avatar`;
ALTER TABLE `user` ADD COLUMN `member_expire_time` DATETIME DEFAULT NULL COMMENT '会员过期时间' AFTER `member_level`;

-- 创建规划记录表
CREATE TABLE IF NOT EXISTS `plan_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `destination` VARCHAR(100) NOT NULL COMMENT '目的地',
    `days` INT DEFAULT 3 COMMENT '天数',
    `budget` VARCHAR(50) DEFAULT NULL COMMENT '预算',
    `preferences` VARCHAR(500) DEFAULT NULL COMMENT '偏好',
    `plan_content` TEXT DEFAULT NULL COMMENT '规划内容',
    `status` INT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规划记录表';
