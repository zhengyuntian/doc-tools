-- ========================================
-- 1. 修改 t_dark_rule_config 表结构
-- ========================================

-- 增加参数类型字段
ALTER TABLE t_dark_rule_config ADD COLUMN param_type varchar(30) DEFAULT 'TEXT' COMMENT '参数类型：TEXT/NUMBER/FLOAT/SELECT/MULTI_SELECT/BOOLEAN/FIXED' AFTER param_unit;

-- 增加可选选项字段（JSON格式）
ALTER TABLE t_dark_rule_config ADD COLUMN param_options text COMMENT '可选选项列表（JSON数组）' AFTER param_type;

-- 增加规则分组字段
ALTER TABLE t_dark_rule_config ADD COLUMN rule_group varchar(50) DEFAULT NULL COMMENT '规则分组标识（同一规则下的多个参数共享同一分组）' AFTER rule_code;

-- 移除唯一索引（软删除场景下会导致冲突）
DROP INDEX uk_rule_param ON t_dark_rule_config;

-- 新增索引
ALTER TABLE t_dark_rule_config ADD INDEX idx_rule_group (rule_group);
ALTER TABLE t_dark_rule_config ADD INDEX idx_param_type (param_type);

-- ========================================
-- 2. 新建 t_dark_rule_scheme（规则方案表）
-- ========================================

CREATE TABLE `t_dark_rule_scheme` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `scheme_name` varchar(100) NOT NULL COMMENT '方案名称',
  `scheme_desc` varchar(500) DEFAULT NULL COMMENT '方案描述',
  `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否默认方案：0-否，1-是',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-禁用，1-启用',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_name` (`creator_name`),
  KEY `idx_is_default` (`is_default`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则方案表';

-- ========================================
-- 3. 新建 t_dark_rule_scheme_item（方案规则配置表）
-- ========================================

CREATE TABLE `t_dark_rule_scheme_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置项ID',
  `scheme_id` bigint(20) NOT NULL COMMENT '所属方案ID',
  `rule_config_id` bigint(20) NOT NULL COMMENT '关联规则配置ID',
  `param_value` varchar(200) DEFAULT NULL COMMENT '用户设定的参数值',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用该规则：0-禁用，1-启用',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_scheme_id` (`scheme_id`),
  KEY `idx_rule_config_id` (`rule_config_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方案规则配置表';

-- 查询验证
SELECT 't_dark_rule_config columns' as table_name, COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 't_dark_rule_config' AND TABLE_SCHEMA = DATABASE();
SELECT 't_dark_rule_scheme created' as result;
SELECT 't_dark_rule_scheme_item created' as result;