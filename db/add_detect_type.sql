-- 批次表新增检测类型字段
ALTER TABLE t_dark_detect_batch
ADD COLUMN scheme_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用方案检测：0-否，1-是' AFTER scheme_id,
ADD COLUMN sensitive_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用敏感词检测：0-否，1-是' AFTER scheme_enabled;

-- 任务表新增检测类型字段
ALTER TABLE t_dark_detect_task
ADD COLUMN scheme_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用方案检测：0-否，1-是' AFTER batch_id,
ADD COLUMN sensitive_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用敏感词检测：0-否，1-是' AFTER scheme_enabled;

-- 检测结果表新增检测类型字段
ALTER TABLE t_dark_detect_result
ADD COLUMN detect_type VARCHAR(20) DEFAULT NULL COMMENT '检测类型：SCHEME-方案检测，SENSITIVE-敏感词检测' AFTER rule_category;

-- 添加索引
CREATE INDEX idx_detect_batch_scheme ON t_dark_detect_batch(scheme_enabled);
CREATE INDEX idx_detect_batch_sensitive ON t_dark_detect_batch(sensitive_enabled);
CREATE INDEX idx_detect_task_scheme ON t_dark_detect_task(scheme_enabled);
CREATE INDEX idx_detect_task_sensitive ON t_dark_detect_task(sensitive_enabled);
CREATE INDEX idx_detect_result_type ON t_dark_detect_result(detect_type);