ALTER TABLE `t_dark_detect_batch` ADD KEY `idx_status` (`status`);

ALTER TABLE `t_dark_detect_batch` ADD KEY `idx_batch_name` (`batch_name`(50));

ALTER TABLE `t_dark_detect_task` ADD KEY `idx_file_name` (`file_name`(100));

ALTER TABLE `t_dark_detect_task` ADD KEY `idx_batch_id` (`batch_id`);

ALTER TABLE `t_dark_detect_task` ADD KEY `idx_status` (`status`);

ALTER TABLE `t_dark_detect_task` ADD KEY `idx_create_time` (`create_time`);
