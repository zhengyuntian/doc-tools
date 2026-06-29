ALTER TABLE `t_dark_detect_batch` ADD COLUMN `scheme_id` bigint(20) DEFAULT NULL COMMENT '关联方案ID' AFTER `batch_name`;
ALTER TABLE `t_dark_detect_batch` ADD INDEX `idx_scheme_id` (`scheme_id`);

ALTER TABLE `t_dark_detect_result` ADD COLUMN `start_offset` int(11) DEFAULT NULL COMMENT '违规起始偏移量' AFTER `paragraph_index`;
ALTER TABLE `t_dark_detect_result` ADD COLUMN `end_offset` int(11) DEFAULT NULL COMMENT '违规结束偏移量' AFTER `start_offset`;