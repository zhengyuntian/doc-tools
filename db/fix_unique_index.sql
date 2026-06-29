-- 修复业务表唯一索引问题（软删除场景下不应有唯一索引）
-- 执行此脚本前请确保数据库中已存在相关表
-- 注意：如果索引不存在，某些语句可能报错，可以忽略

-- 1. 批次表：删除唯一索引（batch_no 应在保存时校验）
ALTER TABLE `t_dark_detect_batch` DROP INDEX `uk_batch_no`;
ALTER TABLE `t_dark_detect_batch` DROP INDEX `uk_batch_no_del`;

-- 2. 任务表：删除唯一索引（task_no 应在保存时校验）
ALTER TABLE `t_dark_detect_task` DROP INDEX `uk_task_no`;
ALTER TABLE `t_dark_detect_task` DROP INDEX `uk_task_no_del`;

-- 3. 规则配置表：删除唯一索引（rule_code+param_key 应在保存时校验）
ALTER TABLE `t_dark_rule_config` DROP INDEX `uk_rule_param`;
ALTER TABLE `t_dark_rule_config` DROP INDEX `uk_rule_param_del`;

-- 4. 敏感词表：删除唯一索引（category_id+word 应在保存时校验）
ALTER TABLE `t_dark_sensitive_word` DROP INDEX `uk_category_word`;
ALTER TABLE `t_dark_sensitive_word` DROP INDEX `uk_category_word_del`;