-- 添加支付方式字段
ALTER TABLE `payment_order` ADD COLUMN `payment_method` VARCHAR(20) DEFAULT 'WECHAT' COMMENT '支付方式：WECHAT-微信支付, ALIPAY-支付宝' AFTER `wx_order_no`;

-- 重命名 wx_order_no 为更通用的名称
ALTER TABLE `payment_order` CHANGE COLUMN `wx_order_no` `third_order_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方支付订单号';

-- 添加支付方式索引
ALTER TABLE `payment_order` ADD INDEX `idx_payment_method` (`payment_method`);
