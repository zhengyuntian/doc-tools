# 暗标检测系统待开发任务清单（v2续）

## 六、检测规则模块改造任务

### 6.1 后端改造
- [ ] 新增按分类查询接口 `GET /demo/darkruleconfig/category/{category}`
- [ ] 新增规则批量启用/禁用接口 `PUT /demo/darkruleconfig/batch/enable`
- [ ] 新增规则初始化接口（预置图片中的检测规则）
- [ ] 新增规则参数校验逻辑

### 6.2 前端改造
- [ ] 规则列表页添加分类筛选 tabs
- [ ] 规则列表按分类分组展示
- [ ] 新增规则参数编辑弹窗
- [ ] 新增规则初始化按钮（一键预置数据）

### 6.3 规则数据初始化（与图片需求对应）
| rule_code | rule_name | rule_category | param_key | param_value | param_unit |
|-----------|-----------|---------------|-----------|-------------|-----------|
| LAYOUT_PAPER_SIZE | 纸张尺寸 | layout | expected_size | A4 | - |
| LAYOUT_COVER | 封面检测 | layout | enabled | 0 | - |
| LAYOUT_TOC | 目录检测 | layout | enabled | 0 | - |
| LAYOUT_HEADER | 页眉检测 | layout | enabled | 0 | - |
| LAYOUT_FOOTER | 页脚检测 | layout | enabled | 0 | - |
| LAYOUT_PAGE_NUM | 页码检测 | layout | enabled | 0 | - |
| FONT_NAME | 字体名称 | font | expected_name | 宋体 | - |
| FONT_SIZE | 字号 | font | expected_size | 14 | pt |
| FONT_BOLD | 加粗检测 | font | allowed | 0 | - |
| FONT_ITALIC | 倾斜检测 | font | allowed | 0 | - |
| FONT_UNDERLINE | 下划线检测 | font | allowed | 0 | - |
| FONT_COLOR | 文字颜色 | font | expected_color | #000000 | - |
| MARGIN_TOP | 上边距 | layout | expected_value | 2.5 | cm |
| MARGIN_LEFT | 左边距 | layout | expected_value | 2 | cm |
| MARGIN_RIGHT | 右边距 | layout | expected_value | 2 | cm |
| MARGIN_BOTTOM | 下边距 | layout | expected_value | 2 | cm |
| LINE_SPACING | 行间距 | layout | expected_value | 30 | pt |
| TEXT_ALIGN | 对齐方式 | layout | expected_value | left | - |
| FIRST_LINE_INDENT | 首行缩进 | layout | expected_value | 2 | 字符 |
| TABLE_FONT_NAME | 表格字体 | table | expected_name | 宋体 | - |
| TABLE_FONT_SIZE | 表格字号 | table | expected_size | 14 | pt |
| TABLE_ALIGN_V | 表格垂直对齐 | table | expected_value | center | - |
| TABLE_ALIGN_H | 表格水平对齐 | table | expected_value | left | - |
| TABLE_AS_IMAGE | 表格图片检测 | table | enabled | 0 | - |

---

## 七、检测结果模块改造任务

### 7.1 后端改造
- [ ] 新增按文件ID查询结果接口 `GET /demo/darkdetectresult/file/{fileId}`
- [ ] 新增结果统计接口（按严重程度分类汇总）
- [ ] 新增结果分页查询（支持按规则编码、是否通过筛选）
- [ ] 确认 idx_task_id、idx_rule_code 索引存在

### 7.2 前端改造
- [ ] 结果列表页添加筛选表单（文件选择、规则分类、是否通过）
- [ ] 结果列表添加违规位置展示（页码、段落）
- [ ] 结果列表添加实际值与期望值对比
- [ ] 新增结果详情弹窗（显示修改建议）
- [ ] 新增结果导出功能（按文件、按批次）

### 7.3 结果输出格式
| 字段 | 说明 | 示例 |
|-----|------|-----|
| is_pass | 是否通过 | 0-不通过，1-通过 |
| severity | 严重程度 | 1-警告，2-一般，3-严重 |
| actual_value | 实际检测值 | "微软雅黑" |
| expected_value | 期望值 | "宋体" |
| remark | 修改建议 | "请将字体改为宋体" |

---

## 八、关联分析模块改造任务

### 8.1 后端改造
- [ ] 新增跨文件分析接口 `POST /demo/darkdetectcrossresult/analyze`
- [ ] 新增按批次查询分析结果接口 `GET /demo/darkdetectcrossresult/batch/{batchId}`
- [ ] 实现敏感词跨文件合并检测逻辑
- [ ] 实现格式一致性检测逻辑
- [ ] 分析类型枚举：SENSITIVE_CROSS / FORMAT_CONSISTENCY

### 8.2 前端改造
- [ ] 关联分析列表页添加分析类型筛选
- [ ] 新增分析结果详情展示（涉及文件列表）
- [ ] 新增跨文件敏感词可视化
- [ ] 新增关联分析报告导出功能

---

## 九、核心检测引擎开发任务（P0优先级）

### 9.1 DOCX 解析服务
- [ ] 引入 Apache POI 依赖
- [ ] 开发 WordDocumentParser 服务
- [ ] 实现段落文本提取
- [ ] 实现字体样式解析（名称、大小、加粗、倾斜、颜色）
- [ ] 实现页面设置解析（纸张大小、页边距）
- [ ] 实现表格内容提取
- [ ] 实现行距、缩进、对齐解析

### 9.2 PDF 解析服务
- [ ] 引入 Apache PDFBox 依赖
- [ ] 开发 PdfDocumentParser 服务
- [ ] 实现文本内容提取
- [ ] 实现页面元数据解析（尺寸、边距）
- [ ] 实现字体信息提取

### 9.3 规则引擎开发
- [ ] 开发 DarkDetectEngine 核心引擎
- [ ] 实现规则加载器（从数据库读取启用规则）
- [ ] 实现版面检测规则执行器
- [ ] 实现字体检测规则执行器
- [ ] 实现排版检测规则执行器
- [ ] 实现敏感词检测规则执行器
- [ ] 实现检测结果组装器
- [ ] 实现批量检测调度器

### 9.4 异步任务处理（队列模式）
- [ ] 创建检测任务队列（使用 ConcurrentLinkedQueue）
- [ ] 创建 DetectTaskConsumer 线程类（单线程消费）
- [ ] 开发队列管理器 DetectQueueManager（管理队列和消费者线程）
- [ ] 实现应用启动时自动启动消费者线程
- [ ] 文件上传成功后自动加入检测队列（接口立即返回）
- [ ] 批次启动检测时批量将文件加入队列
- [ ] 消费者线程从队列取任务执行检测并更新数据库
- [ ] 实现失败重试机制（失败任务重新入队）
- [ ] 实现队列监控（队列大小、处理速度统计）
