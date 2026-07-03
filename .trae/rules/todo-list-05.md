# 暗标检测系统待开发任务清单（v6 - 2026-06-29更新）

## 已完成模块 ✅

### 一、敏感词管理模块 ✅
- [x] 敏感词分类表（t_dark_sensitive_category）
- [x] 敏感词表（t_dark_sensitive_word）
- [x] 敏感词分类管理页面（列表、新增、修改、删除）
- [x] 敏感词管理页面（列表、新增、修改、删除、导入）
- [x] 敏感词导入功能（Excel导入）

### 二、检测规则配置模块 ✅
- [x] 检测规则配置表（t_dark_rule_config）
- [x] 规则分类：layout（版面）、font（字体）、table（表格）、content（内容）
- [x] 规则参数类型：text、number、select、multi_select
- [x] 规则管理页面（列表、新增、修改、删除）
- [x] 规则参数配置（参数类型、参数选项、参数单位）
- [x] 规则初始化数据（预置50条检测规则）

### 三、检测方案管理模块 ✅
- [x] 检测方案表（t_dark_rule_scheme）
- [x] 方案规则配置表（t_dark_rule_scheme_item）
- [x] 方案管理页面（列表、新增、修改、删除）
- [x] 方案规则选择（勾选启用规则）
- [x] 方案规则参数配置（每条规则的参数值）
- [x] 修复编辑方案时规则数据重复问题

### 四、暗标检测批次模块 ✅
- [x] 暗标检测批次表（t_dark_detect_batch）
- [x] 批次管理页面（列表、新增、修改、删除）
- [x] 批次关联检测方案（scheme_id字段）
- [x] 批次上传文件功能（多文件上传）
- [x] 批次启动检测功能
- [x] 批次状态管理（待检测、检测中、全部完成、部分失败）
- [x] 批次统计数据（文件数、通过数、违规数、失败数）

### 五、暗标检测任务模块 ✅
- [x] 暗标检测任务表（t_dark_detect_task）
- [x] 任务管理页面（列表、详情、删除）
- [x] 任务状态管理（排队中、检测中、已完成、失败）
- [x] 任务批次名称显示和筛选
- [x] 任务检测进度显示（当前检测规则/总规则数）
- [x] 任务检测通过比例显示（通过数/总规则数）
- [x] 任务检测结果摘要（列表形式展示违规项）
- [x] 任务启动检测按钮（单个任务启动）
- [x] 任务重试/再次检测按钮
- [x] 任务下载功能（带批注的DOCX/PDF文档）
- [x] 任务自动刷新机制（检测中时每5秒刷新）
- [x] 检测时间、操作人字段更新

### 六、文件规则检测引擎 ✅
- [x] Apache POI 依赖引入
- [x] Apache PDFBox 依赖引入
- [x] WordDocumentParser 服务（DOCX解析）
  - [x] 段落文本提取
  - [x] 字体样式解析（名称、大小、加粗、倾斜、颜色）
  - [x] 页面设置解析（纸张大小、页边距）
  - [x] 表格内容提取
  - [x] 行距、缩进、对齐解析
- [x] PdfDocumentParser 服务（PDF解析）
  - [x] 文本内容提取（支持中文）
  - [x] 页面元数据解析（尺寸、边距）
  - [x] 字体信息提取
- [x] DarkDetectEngine 核心引擎
  - [x] 规则加载器（从方案获取启用规则）
  - [x] 版面检测规则执行器（LayoutRuleExecutor）
  - [x] 字体检测规则执行器（FontRuleExecutor）
  - [x] 表格检测规则执行器（TableRuleExecutor）
  - [x] 标题检测规则执行器（TitleRuleExecutor）
  - [x] 图表检测规则执行器（ChartRuleExecutor）
  - [x] 目录检测规则执行器（TocRuleExecutor）
  - [x] 敏感词检测规则执行器（SensitiveRuleExecutor）
  - [x] 检测结果组装器（位置信息、违规描述）
- [x] 文档批注功能
  - [x] DOCX文档批注（WordAnnotationUtils）
  - [x] PDF文档批注（PdfAnnotationUtils - 标准批注图标）
- [x] 异步任务表设计规范（async-table-design.md）

### 七、敏感词检测独立功能 ✅
- [x] 数据库迁移 SQL（db/add_detect_type.sql）
  - 批次表新增 scheme_enabled、sensitive_enabled 字段
  - 任务表新增 scheme_enabled、sensitive_enabled 字段
  - 检测结果表新增 detect_type 字段（SCHEME/SENSITIVE）
  - 添加相关索引
- [x] 后端实体类改造
  - DarkDetectBatchEntity 新增 schemeEnabled、sensitiveEnabled
  - DarkDetectTaskEntity 新增 schemeEnabled、sensitiveEnabled
  - DarkDetectResultEntity 新增 detectType
- [x] 检测引擎改造
  - detectByScheme() 方案规则检测（原有逻辑）
  - detectBySensitiveWords() 独立敏感词检测（直接加载敏感词）
  - detect() 合并检测流程
- [x] 批次服务校验逻辑
  - 至少启用一种检测类型
  - 启用方案检测时 scheme_id 必填
- [x] 前端页面调整
  - 批次弹窗：检测类型多选、方案条件必填
  - 批次列表：检测类型列、筛选条件
  - 任务列表：检测类型列、筛选条件、详情显示

### 八、PDF批注功能优化 ✅
- [x] 高亮注释添加内容（解决空批注问题）
- [x] 批注位置优化（找不到精确位置时不添加高亮，避免误导）
- [x] 英文标点符号检查显示优化（"英文标点"/"中文标点"）
- [x] 段前段后间距检查 NullPointerException 修复
- [x] 批注内容构建优化（确保所有情况都有完整内容）

---

## 待验证模块 ⏳

### 八、OCR识别服务 ✅
- [x] OCR服务选型（SmartJavaAI + PP-OCRv5 + SLANET）
- [x] 本地OCR服务集成（DJL + OnnxRuntime）
- [x] 图片文字识别功能实现
- [x] 图片表格结构识别功能实现
- [x] OCR与规则引擎对接
- [x] 禁止表格图片检测规则（TABLE_IMAGE_FORBIDDEN）
- [x] SmartJavaAI Config类覆盖（解决PyTorch强制加载问题）
- [x] 模型加载配置（文本检测+方向分类+文字识别+表格识别）

### 关联分析模块 ⏳
- [x] 关联分析结果表（t_dark_detect_cross_result）
- [x] 异步任务表审计字段修复（移除creator_name/updater_name/del_flag）
- [x] 软删除拦截器排除配置（DarkSoftDeleteInterceptor）
- [x] 关联分析结果列表页（批次名称显示）
- [x] 执行关联分析接口
- [ ] 敏感词跨文件合并检测逻辑验证
- [ ] 格式一致性检测逻辑验证
- [ ] 分析结果详情展示优化
- [ ] 跨文件敏感词可视化
- [ ] 关联分析报告导出功能

---

## 待完成模块 📋

### 九、体验优化（P2）
- [ ] 实时进度WebSocket推送
- [ ] 检测进度条动画优化
- [ ] 结果导出美化（PDF报告）
- [ ] 敏感词可视化（词云图）
- [ ] 批量操作优化（批量重试、批量删除）
- [ ] 检测结果对比视图（实际值vs期望值）

### 十、运维支持（P3）
- [ ] 检测日志完善
- [ ] 错误处理优化（异常分类、友好提示）
- [ ] 性能监控（检测耗时统计）
- [ ] 数据备份策略
- [ ] 队列监控（队列大小、处理速度）

---

## 数据库表现状

| 表名 | 状态 | 说明 |
|-----|------|-----|
| t_dark_sensitive_category | ✅ 已完成 | 敏感词分类 |
| t_dark_sensitive_word | ✅ 已完成 | 敏感词 |
| t_dark_rule_config | ✅ 已完成 | 检测规则配置（50条预置数据） |
| t_dark_rule_scheme | ✅ 已完成 | 检测方案（2个方案） |
| t_dark_rule_scheme_item | ✅ 已完成 | 方案规则项（每个方案50条规则） |
| t_dark_detect_batch | ✅ 已完成 | 检测批次 |
| t_dark_detect_task | ✅ 已完成 | 检测任务 |
| t_dark_detect_result | ✅ 已完成 | 检测结果详情 |
| t_dark_detect_cross_result | ✅ 已完成 | 关联分析结果（异步任务表） |

---

## 下一步优先任务

1. **OCR功能测试验证**
   - OCR服务选型确认
   - 图片文字识别测试
   - 与规则引擎对接

2. **关联分析功能验证**
   - 敏感词跨文件合并检测逻辑测试
   - 格式一致性检测逻辑测试
   - 分析结果可视化优化

3. **体验优化**
   - 实时进度推送
   - 导出报告美化

---

## 技术栈确认

- 后端：Spring Boot 4.0.5 + JDK17 + MyBatis-Plus 3.5.16 + Shiro 2.1.0
- 前端：Vue3 3.5.18 + Vite 5.4.19 + TypeScript 5.7 + Element-Plus 2.10.5
- 数据库：MySQL 8
- 文档解析：Apache POI 5.2.5
- PDF解析：Apache PDFBox 3.0.x
- OCR：SmartJavaAI 1.0.23 + DJL 0.34.0 + OnnxRuntime 1.20.0