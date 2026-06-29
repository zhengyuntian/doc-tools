# 数据库规则

## 表设计规范
- 字符集：`utf8mb4`，引擎：`InnoDB`
- 系统表前缀 `sys_`，业务表前缀 `t_dark_`
- 必须包含字段：`id`（主键，bigint auto_increment）、`create_time` / `update_time`（datetime）、`del_flag`（软删除 0/1）
- 业务表额外包含：`creator_name`、`updater_name`（varchar 50）

## 字段命名
- 使用下划线命名法，如 `batch_no`、`file_path`
- 状态字段使用 `tinyint`，并在注释中说明每个值含义
- JSON 字段使用 `text` 类型，如 `result_summary`、`cross_analysis_result`
- 布尔语义字段使用 `0/1`，如 `is_related`、`enabled`

## 索引规范
- 主键名为 `id`
- 唯一索引前缀 `uk_`，如 `uk_batch_no`
- 普通索引前缀 `idx_`，如 `idx_create_time`、`idx_batch_id`
- 外键关联字段必须建索引，如 `batch_id`、`task_id`
- 状态、时间、创建人等高频查询字段建议建索引