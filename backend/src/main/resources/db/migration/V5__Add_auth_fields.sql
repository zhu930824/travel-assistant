-- 添加微信登录相关字段
ALTER TABLE `user` ADD COLUMN `wechat_openid` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID' AFTER `phone`;
ALTER TABLE `user` ADD COLUMN `wechat_unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信UnionID' AFTER `wechat_openid`;

-- 添加唯一索引
ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);
ALTER TABLE `user` ADD UNIQUE KEY `uk_wechat_openid` (`wechat_openid`);
