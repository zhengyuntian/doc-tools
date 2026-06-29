-- ========================================
-- 异步任务表审计字段清理
-- 异步检测任务生成的数据表不应该包含审计字段
-- t_dark_detect_cross_result 表正确结构确认
-- ========================================

-- 当前表结构（正确结构）：
-- id, batch_id, analysis_type, analysis_name, involved_files, 
-- actual_value, expected_value, is_pass, severity, remark, create_time

-- 此表由异步任务自动填充，无人工操作，不需要 creator_name/updater_name/del_flag

-- 检查是否存在多余的审计字段，如果有则删除
SET FOREIGN_KEY_CHECKS = 0;

-- 删除 creator_name（如果存在）
ALTER TABLE t_dark_detect_cross_result DROP COLUMN IF EXISTS creator_name;

-- 删除 updater_name（如果存在）
ALTER TABLE t_dark_detect_cross_result DROP COLUMN IF EXISTS updater_name;

-- 删除 update_time（如果存在）
ALTER TABLE t_dark_detect_cross_result DROP COLUMN IF EXISTS update_time;

-- 删除 del_flag（如果存在）
ALTER TABLE t_dark_detect_cross_result DROP COLUMN IF EXISTS del_flag;

SET FOREIGN_KEY_CHECKS = 1;

-- 验证最终表结构
DESCRIBE t_dark_detect_cross_result;