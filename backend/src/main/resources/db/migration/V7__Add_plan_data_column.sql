-- V7__Add_plan_data_column.sql

ALTER TABLE plan_record
ADD COLUMN plan_data JSON COMMENT '结构化行程数据' AFTER plan_content;
