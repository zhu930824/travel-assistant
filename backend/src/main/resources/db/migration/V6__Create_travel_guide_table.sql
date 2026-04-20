-- 创建攻略表
CREATE TABLE IF NOT EXISTS `travel_guide` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '攻略ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `destination` VARCHAR(100) NOT NULL COMMENT '目的地',
    `content` TEXT NOT NULL COMMENT '攻略内容（富文本HTML）',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    `images` JSON DEFAULT NULL COMMENT '图片列表JSON',
    `tags` JSON DEFAULT NULL COMMENT '标签列表JSON',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
    `status` INT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_destination` (`destination`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅行攻略表';

-- 创建攻略点赞表
CREATE TABLE IF NOT EXISTS `guide_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `guide_id` BIGINT NOT NULL COMMENT '攻略ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_guide_user` (`guide_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='攻略点赞表';

-- 创建攻略收藏表
CREATE TABLE IF NOT EXISTS `guide_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `guide_id` BIGINT NOT NULL COMMENT '攻略ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_guide_user` (`guide_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='攻略收藏表';
