-- 为检测结果表添加缺失的审计字段
ALTER TABLE `t_dark_detect_result` ADD COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间' AFTER `create_time`;
ALTER TABLE `t_dark_detect_result` ADD COLUMN `del_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）' AFTER `update_time`;
