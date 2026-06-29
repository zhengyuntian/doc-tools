# 暗标检测系统待开发任务清单（v4）

## 一、最新调整规则

### 1.1 前端页面调整
- 批次列表右侧操作菜单添加"任务列表"入口
- 任务列表展示每个文件信息及处理状态
- 敏感词分类和敏感词管理合并到同一页面 ✅ 已完成

### 1.2 数据库设计调整
- **任务表 = 文件表**：一个文件即一个处理任务，不再分开
- 原 t_dark_detect_task 改造为 t_dark_detect_file（承载文件信息和任务状态）

### 1.3 审计字段处理（MyBatis拦截器）✅ 已完成
- 新增：自动赋值 creator_name、updater_name、create_time、update_time、del_flag=0
- 更新：自动赋值 updater_name、update_time
- 删除：自动更新 del_flag=1 ✅ 已完成
- 查询：自动过滤 del_flag=0 ✅ 已完成

---

## 二、批次管理模块改造任务 ✅ 基础功能已完成

### 2.1 后端改造 ✅ 已完成
- [x] 新增条件查询接口（按批次名称、状态、时间范围）
- [x] 新增文件上传接口 `POST /demo/darkdetectbatch/{id}/upload`（接口立即返回，文件加入检测队列）
- [x] 新增启动检测接口 `POST /demo/darkdetectbatch/{id}/detect`
- [x] 新增批次统计接口 `GET /demo/darkdetectbatch/{id}/statistics`
- [x] 修改保存逻辑：自动生成 batch_no（BATCH+时间戳）
- [x] 修改删除逻辑：级联更新关联文件的 del_flag=1

### 2.2 前端改造 ✅ 已完成
- [x] 批次列表添加筛选表单（名称、状态、时间）
- [x] 批次列表添加统计卡片（总批次/处理中/已完成）
- [x] **批次操作栏新增"任务列表"按钮（跳转到任务列表）**
- [x] 批次操作栏新增"上传文件"、"启动检测"按钮
- [x] 新增文件上传组件（拖拽、多文件）
- [x] 新增检测进度展示组件
- [x] 新增/修改弹窗：批次编号由后端生成，新增时不展示
- [x] 关联分析开关添加说明文字（跨文件关联分析，生成综合报告）

### 2.3 数据库改造 ✅ 已完成
- [x] 批次表添加 batch_no 自动生成逻辑（后端 Service 层实现）
- [x] 确认 idx_status 索引存在（已执行 db/add_indexes.sql）

---

## 三、文件/任务一体化模块改造任务

### 3.1 数据库调整 ✅ 已完成
- [x] 废弃 t_dark_detect_task 表（保留并改造）
- [x] 改造 t_dark_detect_file 表（实际使用 t_dark_detect_task）
  - status（任务状态：0排队/1检测中/2完成/3失败）
  - total_rules、pass_rules、fail_rules
  - result_summary、error_msg
- [x] 添加 idx_batch_id、idx_status、idx_create_time 索引

### 3.2 后端改造 ✅ 已完成
- [x] 新增文件上传接口（关联批次，接口立即返回，文件加入检测队列）
- [x] 新增文件列表查询接口（按批次ID、文件类型、状态筛选）
- [x] 新增文件下载接口 `GET /demo/darkdetecttask/{id}/download`
- [x] 新增文件删除接口 `DELETE /demo/darkdetecttask`（批量软删除）
- [x] 新增任务重试接口 `POST /demo/darkdetecttask/{id}/retry`
- [x] 新增文件详情接口（含检测进度）

### 3.3 前端改造 ✅ 已完成
- [x] 新增文件/任务列表页 `darkdetecttask.vue`
- [x] **文件列表显示处理状态标签（排队中/检测中/已完成/失败）**
- [x] 文件列表显示检测进度（pass_rules/total_rules）
- [x] 文件列表显示违规数量（fail_rules）
- [x] 文件操作栏：下载、查看结果、重试、删除
- [x] 新增文件类型图标（DOCX/PDF）
- [x] 移除手动新增/修改功能（文件通过批次上传创建）
- [x] 详情弹窗改为只读展示

### 3.4 状态流转
```
QUEUED(0) → PROCESSING(1) → COMPLETED(2)
    ↓            ↓
  FAILED(3) ←──┘
```

---

## 四、审计字段 MyBatis 拦截器开发 ✅ 主要功能已完成

### 4.1 后端开发
- [x] 创建 BaseEntity 抽象类（包含 creator_name、updater_name、create_time、update_time、del_flag 字段）
- [x] 创建 DarkSoftDeleteInterceptor（MyBatis 拦截器）
- [x] 实现 Select 拦截：自动追加 where del_flag=0
- [x] 实现 Delete 拦截：转换为 update del_flag=1（通过 Service 层实现）
- [x] 修改所有 Entity 继承 BaseEntity

### 4.2 涉及改造 ✅ 已完成
- [x] DarkDetectBatchEntity 继承 BaseEntity
- [x] DarkDetectTaskEntity 继承 BaseEntity
- [x] DarkRuleConfigEntity 继承 BaseEntity
- [x] DarkSensitiveCategoryEntity 继承 BaseEntity
- [x] DarkSensitiveWordEntity 继承 BaseEntity

### 4.3 软删除实现 ✅ 已完成
- [x] 所有 `t_dark_` 前缀业务表删除操作更新 del_flag=1
- [x] 删除操作通过 CrudServiceImpl.delete() 方法实现软删除
- [x] 移除数据库唯一索引（软删除场景下会导致冲突）
- [x] 唯一性校验移至 Service 层处理

---

## 五、敏感词管理模块改造任务 ✅ 已完成

### 5.1 数据库保持 ✅ 已完成
- 维持 t_dark_sensitive_category 和 t_dark_sensitive_word 两表
- 移除 uk_category_word 唯一索引，避免软删除冲突

### 5.2 后端改造 ✅ 已完成
- [x] 新增按分类查询敏感词接口
- [x] 新增敏感词搜索接口（模糊查询，支持 word、categoryId、enabled 参数）
- [x] 分类删除时校验是否有关联敏感词
- [x] 敏感词唯一性校验在 Service 层实现

### 5.3 前端改造 ✅ 已完成
- [x] **合并 darksensitivecategory.vue 和 darksensitiveword.vue 为 sensitive.vue**
- [x] **页面布局：顶部搜索 + 敏感词列表（移除左侧分类树）**
- [x] **新增敏感词分类按钮（顶部）**
- [x] **新增敏感词时弹窗显示分类下拉列表**
- [x] 敏感词列表支持搜索（敏感词模糊搜索）
- [x] 敏感词列表支持分类筛选（下拉选择）
- [x] 敏感词列表支持状态筛选（启用/禁用）
- [x] 敏感词列表支持批量删除
- [x] 敏感词列表按更新时间倒序排序
- [ ] 新增敏感词批量导入弹窗

### 5.4 页面结构示意
```
┌─────────────────────────────────────────────────────────┐
│ 敏感词管理                            [+新增分类] [+新增敏感词] [删除] │
├─────────────────────────────────────────────────────────┤
│ [搜索敏感词] [所属分类▼] [状态▼]        [查询] [重置]              │
├─────────────────────────────────────────────────────────┤
│ 敏感词   │ 所属分类 │ 状态 │ 创建时间 │ 操作                    │
│ 投标     │ 投标招标 │ 启用 │ 2026-06-25│ [修改] [删除]          │
│ 招标     │ 投标招标 │ 启用 │ 2026-06-25│ [修改] [删除]          │
└─────────────────────────────────────────────────────────┘
```

### 5.5 时间格式化 ✅ 已完成
- [x] 后端 DTO 字段添加 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
- [x] 敏感词列表按更新时间倒序排序

---

## 六、规则配置管理模块 ✅ 已完成

### 6.1 数据库设计 ✅ 已完成
- [x] 创建 t_dark_rule_config 表（规则定义表）
- [x] 支持多种参数类型：TEXT、NUMBER、FLOAT、SELECT、MULTI_SELECT、BOOLEAN、FIXED
- [x] 规则分类字段：rule_category（layout/font/table/chart/title/toc/self_check/page_count/sensitive/cross）
- [x] 生成规则内置数据SQL脚本

### 6.2 后端改造 ✅ 已完成
- [x] 规则分页查询接口（支持分类筛选）
- [x] 规则新增/修改/删除接口
- [x] 固定值规则禁止修改和删除（Service层校验）
- [x] 参数类型动态处理

### 6.3 前端改造 ✅ 已完成
- [x] 规则配置列表页 `darkruleconfig.vue`
- [x] 规则新增/修改弹窗 `darkruleconfig-add-or-update.vue`
- [x] 根据参数类型动态渲染表单控件
- [x] 固定值规则禁止修改和删除

---

## 七、规则方案管理模块 ✅ 已完成

### 7.1 数据库设计 ✅ 已完成
- [x] 创建 t_dark_rule_scheme 表（规则方案表）
- [x] 创建 t_dark_rule_scheme_item 表（方案规则配置表）
- [x] 支持方案设为默认（is_default 字段）

### 7.2 后端改造 ✅ 已完成
- [x] 方案分页查询接口
- [x] 方案新增/修改/删除接口
- [x] 方案详情接口（含关联规则配置项）
- [x] 设为默认方案接口
- [x] 方案保存时自动清除其他默认方案
- [x] 修复 convertToDTO 方法不存在问题

### 7.3 前端改造 ✅ 已完成
- [x] 规则方案列表页 `darkrulescheme.vue`
- [x] 方案新增/修改弹窗 `darkrulescheme-add-or-update.vue`
- [x] 规则按分类分组显示（el-tabs）
- [x] 规则选择表格（el-table）
- [x] 默认值自动填充（页边距、缩进、字符间距等）
- [x] SELECT/MULTI_SELECT 默认选中第一个选项
- [x] 数据库值中文映射（left→左对齐、black→黑色、portrait→纵向等）
- [x] BOOLEAN 类型开关修复（只能关不能开问题）
- [x] 方案列表默认状态显示（非默认标签）

---

## 八、待处理问题

### 8.1 数据库脚本
- [x] 修复唯一索引问题（已移除 uk_category_word 等索引）
- [ ] 确认所有业务表的索引完整

### 8.2 代码清理
- [ ] 移除 DarkSoftDeleteInterceptor 中的 beforeUpdate 方法（MyBatis-Plus DELETE 语句不触发此方法）
- [ ] 确认所有调试日志已移除

---

## 九、已完成功能汇总

### 后端
1. ✅ 敏感词分页查询（支持敏感词搜索、分类筛选、状态筛选）
2. ✅ 敏感词新增/修改/删除
3. ✅ 敏感词唯一性校验（Service 层）
4. ✅ 所有业务表软删除（t_dark_ 前缀表）
5. ✅ 敏感词列表按更新时间倒序排序
6. ✅ 时间字段格式化输出（yyyy-MM-dd HH:mm:ss）
7. ✅ 规则配置 CRUD 接口
8. ✅ 规则方案 CRUD 接口
9. ✅ 规则方案详情接口（含关联规则项）
10. ✅ 方案设为默认功能
11. ✅ 规则参数类型支持（TEXT/NUMBER/FLOAT/SELECT/MULTI_SELECT/BOOLEAN/FIXED）
12. ✅ 批次管理基础功能（CRUD + 条件查询）
13. ✅ 批次编号自动生成（BATCH+时间戳）
14. ✅ 批次级联删除（删除批次时级联更新关联文件的 del_flag）

### 前端
1. ✅ 敏感词管理页面（敏感词 + 分类合并）
2. ✅ 敏感词搜索功能
3. ✅ 分类筛选功能
4. ✅ 状态筛选功能
5. ✅ 新增分类弹窗
6. ✅ 新增敏感词弹窗
7. ✅ 修改敏感词弹窗
8. ✅ 批量删除功能
9. ✅ 规则配置列表页
10. ✅ 规则配置新增/修改弹窗
11. ✅ 规则方案列表页（含默认状态显示）
12. ✅ 规则方案新增/修改弹窗（含规则选择表格）
13. ✅ 规则按分类分组显示
14. ✅ 默认值自动填充
15. ✅ 数据库值中文映射显示
16. ✅ 批次管理列表页（含筛选表单）
17. ✅ 批次新增/修改弹窗（批次编号自动生成、新增时不展示）
18. ✅ 关联分析开关说明文字（跨文件关联分析，生成综合报告）
19. ✅ 任务列表入口按钮（跳转到任务列表页）
20. ✅ 图标修复（FileText/FileWord → Document/FileWord2）
