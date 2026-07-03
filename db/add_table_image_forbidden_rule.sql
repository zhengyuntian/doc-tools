-- ========================================
-- 添加禁止表格以图片格式插入的检测规则
-- ========================================

-- 插入新规则
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_IMAGE_FORBIDDEN', '禁止表格以图片格式插入', 'table', 'TABLE_IMAGE_FORBIDDEN', 'forbid_image_table', '禁止图片表格', 'true', '', 'BOOLEAN', NULL, 37, 1, 'admin', NOW(), 0);

-- 更新后续规则的sort_order
UPDATE t_dark_rule_config SET sort_order = 41 WHERE rule_code = 'CHART_CHECK';

-- 验证插入结果
SELECT rule_code, rule_name, rule_category, param_key, param_value, sort_order, enabled
FROM t_dark_rule_config
WHERE rule_category = 'table' OR rule_code = 'CHART_CHECK'
ORDER BY sort_order;