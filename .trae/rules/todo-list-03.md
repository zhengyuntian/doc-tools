# 暗标检测系统待开发任务清单（v2续）

## 十、菜单权限配置

### 10.1 当前已有菜单
```
检测中心（pid=1067246875800000035）
├── 暗标检测批次表（demo/darkdetectbatch）
├── 关联分析结果表（demo/darkdetectcrossresult）
├── 单文件检测结果详情表（demo/darkdetectresult）
├── 暗标检测任务表（demo/darkdetecttask）  ← 待废弃
├── 检测规则配置表（demo/darkruleconfig）
├── 敏感词分类表（demo/darksensitivecategory）  ← 合并到 sensitive
└── 敏感词表（demo/darksensitiveword）  ← 合并到 sensitive
```

### 10.2 需要调整的菜单
| 调整 | 菜单 | URL路径 | 权限 |
|------|------|---------|------|
| 废弃 | 暗标检测任务表 | demo/darkdetecttask | 移除 |
| 合并 | 敏感词分类+敏感词 | demo/sensitive | demo:sensitive:* |
| 新增 | 文件/任务管理 | demo/darkdetectfile | demo:darkdetectfile:* |

### 10.3 需要新增的菜单
| 菜单名称 | URL路径 | 权限字符串 | 父级 |
|---------|---------|-----------|------|
| 文件管理 | demo/darkdetectfile | demo:darkdetectfile:page,info,upload,download,delete,retry | 检测中心 |
| 敏感词管理 | demo/sensitive | demo:sensitive:* | 检测中心 |

### 10.4 需要新增的按钮权限
| 按钮 | 权限字符串 |
|------|-----------|
| 批次-任务列表 | demo:darkdetectbatch:filelist |
| 批次-上传文件 | demo:darkdetectbatch:upload |
| 批次-启动检测 | demo:darkdetectbatch:detect |
| 文件-下载 | demo:darkdetectfile:download |
| 文件-重试 | demo:darkdetectfile:retry |
| 敏感词-新增分类 | demo:sensitive:addcategory |
| 敏感词-导入 | demo:sensitive:import |
| 规则-初始化 | demo:darkruleconfig:init |

---

## 十一、接口清单

### 11.1 批次管理接口
| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 分页查询 | GET | /demo/darkdetectbatch/page | 支持条件筛选 |
| 详情 | GET | /demo/darkdetectbatch/{id} | - |
| 新增 | POST | /demo/darkdetectbatch | - |
| 修改 | PUT | /demo/darkdetectbatch | - |
| 删除 | DELETE | /demo/darkdetectbatch | 批量 |
| 导出 | GET | /demo/darkdetectbatch/export | - |
| 上传文件 | POST | /demo/darkdetectbatch/{id}/upload | 多文件 |
| 启动检测 | POST | /demo/darkdetectbatch/{id}/detect | - |
| 获取统计 | GET | /demo/darkdetectbatch/{id}/statistics | - |

### 11.2 文件/任务一体化接口
| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 分页查询 | GET | /demo/darkdetectfile/page | 支持批次、状态筛选 |
| 详情 | GET | /demo/darkdetectfile/{id} | - |
| 下载 | GET | /demo/darkdetectfile/{id}/download | - |
| 删除 | DELETE | /demo/darkdetectfile | 批量软删除 |
| 重试 | POST | /demo/darkdetectfile/{id}/retry | - |
| 导出 | GET | /demo/darkdetectfile/export | - |

### 11.3 检测结果接口
| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 分页查询 | GET | /demo/darkdetectresult/page | 按文件筛选 |
| 按文件查 | GET | /demo/darkdetectresult/file/{fileId} | - |
| 导出 | GET | /demo/darkdetectresult/export | - |

### 11.4 规则配置接口
| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 分页查询 | GET | /demo/darkruleconfig/page | - |
| 按分类查 | GET | /demo/darkruleconfig/category/{category} | - |
| 批量启用 | PUT | /demo/darkruleconfig/batch/enable | - |
| 初始化规则 | POST | /demo/darkruleconfig/init | 预置数据 |

### 11.5 敏感词管理接口（合并后）
| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 分类分页 | GET | /demo/sensitive/category/page | - |
| 分类新增 | POST | /demo/sensitive/category | - |
| 分类修改 | PUT | /demo/sensitive/category | - |
| 分类删除 | DELETE | /demo/sensitive/category | - |
| 敏感词分页 | GET | /demo/sensitive/word/page | 支持分类筛选 |
| 敏感词新增 | POST | /demo/sensitive/word | - |
| 敏感词导入 | POST | /demo/sensitive/word/import | Excel 导入 |

---

## 十二、数据库变更

### 12.1 表结构调整
- [ ] 废弃 t_dark_detect_task（保留数据，停止使用）
- [ ] 改造 t_dark_detect_file 新增字段：
  - status（任务状态）
  - total_rules、pass_rules、fail_rules
  - result_summary、error_msg

### 12.2 索引检查
- [ ] t_dark_detect_batch: idx_status
- [ ] t_dark_detect_file: idx_batch_id, idx_status, idx_create_time
- [ ] t_dark_detect_result: idx_task_id, idx_rule_code
- [ ] t_dark_rule_config: idx_rule_code, idx_category

---

## 十三、开发优先级排序

### Phase 1：基础设施（P0）
1. **审计字段 MyBatis 拦截器**（影响所有模块）
2. **文件/任务表合并**（数据库结构调整）
3. **BaseEntity 改造**（所有 Entity 继承）

### Phase 2：MVP 核心功能（P0）
1. 检测引擎开发（DOCX解析 + 规则执行）
2. 批次管理改造（上传 + 启动检测）
3. 文件/任务管理（一体化后）
4. 结果展示开发

### Phase 3：业务流程完善（P1）
1. 敏感词页面合并
2. 关联分析功能
3. 规则配置优化
4. 规则初始化

### Phase 4：体验优化（P2）
1. 实时进度展示
2. 结果导出美化
3. 敏感词可视化
4. 批量操作优化

### Phase 5：运维支持（P3）
1. 日志完善
2. 错误处理优化
3. 性能监控
4. 数据备份
