-- 创建对话Checkpoint表
CREATE TABLE IF NOT EXISTS `conversation_checkpoint` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `destination` VARCHAR(100) DEFAULT NULL COMMENT '目的地',
    `days` INT DEFAULT NULL COMMENT '天数',
    `budget` VARCHAR(50) DEFAULT NULL COMMENT '预算',
    `preferences` VARCHAR(500) DEFAULT NULL COMMENT '偏好',
    `human_input_mode` VARCHAR(20) DEFAULT 'TERMINATE' COMMENT '人工输入模式: ALWAYS/NEVER/TERMINATE',
    `current_round` INT DEFAULT 0 COMMENT '当前对话轮数',
    `last_speaker` VARCHAR(50) DEFAULT NULL COMMENT '最后发言者',
    `terminated` TINYINT(1) DEFAULT 0 COMMENT '是否已终止',
    `termination_reason` VARCHAR(200) DEFAULT NULL COMMENT '终止原因',
    `messages` JSON DEFAULT NULL COMMENT '消息列表JSON',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-活跃/paused-暂停/completed-完成',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话Checkpoint表';
