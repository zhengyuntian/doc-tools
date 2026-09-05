# doc-tools 暗标检测系统

基于 **renren-security** 快速开发平台构建的投标文件（暗标）智能检测系统，采用前后端分离架构。
系统可对 DOCX / PDF 格式的投标文件进行自动化格式检测、敏感词检测与跨文件关联分析，并生成带批注的检测文档。

- 基座：renren-security（SpringBoot4.x + Shiro + MyBatis-Plus + Vue3 + TypeScript + Element Plus）
- 演示地址：http://demo.open.renren.io/renren-security （账号密码：admin/admin）

<br>

## 一、系统功能总览

```
暗标检测系统
├─ 检测中心
│  ├─ 批次管理        批次 CRUD、文件上传、启动检测、批次统计、级联删除
│  ├─ 任务列表        文件即任务，状态流转、检测进度、重试、停止、下载带批注文档
│  ├─ 检测结果        单文件违规明细（实际值 vs 期望值、修改建议、严重程度）、导出
│  └─ 关联分析        跨文件敏感词重复分析 + 格式一致性分析、按批次查询、导出
├─ 检测配置
│  ├─ 检测规则配置    50+ 预置规则，7 大分类，多种参数类型，启用/禁用
│  ├─ 检测方案管理    方案-规则组配，参数覆盖，默认方案
│  └─ 敏感词管理      分类 + 敏感词合并页面，搜索/筛选/批量删除
└─ 系统管理           用户、角色、菜单、部门、字典、参数、日志、定时任务、文件存储（renren 基座自带）
```

<br>

## 二、技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 4.0.5 |
| 安全框架 | Apache Shiro | 2.1.0 |
| 持久层 | MyBatis-Plus | 3.5.16 |
| 定时任务 | Quartz | 2.5 |
| 连接池 | Druid | 1.2 |
| JDK | Java | 17 |
| 前端框架 | Vue3 + Vite + TypeScript | 3.5.18 / 5.4.19 / 5.7 |
| UI 组件 | Element Plus + Pinia | 2.10.5 / 2.3.1 |
| DOCX 解析 | Apache POI | 5.2.5 |
| PDF 解析 | Apache PDFBox | 3.0.2 |
| OCR 识别 | SmartJavaAI + DJL + OnnxRuntime | 1.0.23 / 0.34.0 / 1.20.0 |
| OCR 模型 | PP-OCRv5（检测/识别）、PP-OCR Mobile V2（方向分类）、SLANET+（表格识别） | 本地部署 |
| 数据库 | MySQL（支持 Oracle / DM8 / PostgreSQL / SQLServer） | 8.0+ |

<br>

## 三、项目结构

```
doc-tools
├─renren-common             公共模块（工具类、注解、异常、校验）
├─renren-dynamic-datasource 动态数据源
├─renren-admin              管理后台
│  ├─db                     数据库 SQL 脚本（mysql.sql + 业务表脚本）
│  ├─modules
│  │  ├─demo                ★ 暗标检测核心业务模块
│  │  │  ├─controller       批次/任务/结果/关联分析/规则/方案/敏感词/统计 接口
│  │  │  ├─service(.impl)   业务逻辑层
│  │  │  ├─dao              数据访问层
│  │  │  ├─entity / dto / excel
│  │  │  ├─engine           ★ 检测引擎
│  │  │  │  ├─DarkDetectEngine      检测入口（方案检测 + 敏感词检测）
│  │  │  │  ├─CrossFileAnalyzer     跨文件关联分析
│  │  │  │  ├─DocumentParser / OCRService / RuleExecutor  接口
│  │  │  │  ├─impl          DocxDocumentParser、PdfDocumentParser、
│  │  │  │  │               PaddleOCRService、Layout/Font/Table/Chart/Title/Toc/Sensitive 规则执行器
│  │  │  │  └─model         ParsedDocument/ParsedPageSetup/ParsedParagraph/ParsedRun/ParsedTable/ParsedImage
│  │  │  └─util             DocxAnnotationUtils（Word 批注）、PdfAnnotationUtils（PDF 批注）
│  │  ├─job / log / oss / security / sys
│  └─resources              mapper、application.yml、public
├─renren-api                API 服务
├─renren-generator          代码生成器
├─renren-ui                 Vue3 前端工程
│  └─src/views/demo         darkdetectbatch、darkdetecttask、darkdetectresult、
│                           darkdetectcrossresult、darkruleconfig、darkrulescheme、sensitive
├─models                    OCR 本地模型（PP-OCRv5 det/rec、方向分类、SLANET+）
├─docker                    Dockerfile、各环境配置
├─docs                      文档
└─db                        业务表结构与初始化数据 SQL
```

<br>

## 四、核心架构

### 4.1 检测引擎流程

```
文件上传 / 启动检测（接口立即返回）
        │
        ▼
DarkDetectTaskServiceImpl（后台线程异步执行，状态轮询更新进度）
        │
        ▼
DarkDetectEngine.detect(taskId, filePath, fileType, schemeId, crossEnabled)
        │
        ├─ DocumentParser.parse ──► ParsedDocument（段落/字体/页面设置/表格/行距/缩进/对齐）
        │        ├─ DOCX → DocxDocumentParser（Apache POI）
        │        └─ PDF  → PdfDocumentParser（Apache PDFBox）
        │
        ├─ 方案检测：加载方案启用规则 → RuleExecutor.execute(parsedDocument, rule)
        │        ├─ LayoutRuleExecutor    纸张、页边距、页眉页脚、页码、对齐、缩进、行距
        │        ├─ FontRuleExecutor      字体名称、字号、加粗、倾斜、下划线、颜色
        │        ├─ TableRuleExecutor     表格字体/字号/对齐、表格图片化检测
        │        ├─ ChartRuleExecutor     图表规范
        │        ├─ TitleRuleExecutor     标题规范
        │        ├─ TocRuleExecutor       目录规范
        │        └─ SensitiveRuleExecutor 敏感词命中检测（支持 OCR 图片文字）
        │
        ├─ 敏感词检测：加载启用敏感词 → 逐段匹配（含 OCR 识别文本）
        │
        └─ OCRService → PaddleOCRService（SmartJavaAI + OnnxRuntime，本地 PP-OCRv5 / SLANET+ 模型）
                 ├─ recognizeText  图片文字识别
                 └─ recognizeTable 图片表格识别

检测完成 → 检测结果落库（t_dark_detect_result）→ 批次开启关联分析时 → CrossFileAnalyzer.analyze(batchId)
        ├─ SENSITIVE_CROSS     敏感词跨文件重复分析
        └─ FORMAT_CONSISTENCY  多文件格式一致性分析
```

### 4.2 任务状态流转

```
QUEUED(0 排队中) → PROCESSING(1 检测中) → COMPLETED(2 已完成)
                        │
                        └──────────► FAILED(3 失败) ──retry──► 重新排队
```

### 4.3 审计字段与软删除（MyBatis 拦截器）

- `BaseEntity` 抽象类统一携带 `creator_name`、`updater_name`、`create_time`、`update_time`、`del_flag`
- `FieldMetaObjectHandler` INSERT/UPDATE 自动填充审计字段
- `DarkSoftDeleteInterceptor` 自动追加 `del_flag=0` 查询条件、DELETE 转 UPDATE `del_flag=1`（异步任务表 `t_dark_detect_cross_result` 已排除）
- 唯一性校验移至 Service 层，数据库层不建唯一索引（避免软删除冲突）

### 4.4 带批注文档下载

检测完成且存在违规项时，任务列表支持下载批注文档：
- DOCX：`DocxAnnotationUtils` 基于 XWPFComments 在违规文本/图片位置插入批注
- PDF：`PdfAnnotationUtils` 基于 PDAnnotationText + PDAnnotationPopup 添加批注

<br>

## 五、数据库设计

| 表名 | 说明 | 状态 |
|------|------|------|
| t_dark_sensitive_category | 敏感词分类 | ✅ |
| t_dark_sensitive_word | 敏感词 | ✅ |
| t_dark_rule_config | 检测规则配置（50+ 条预置规则） | ✅ |
| t_dark_rule_scheme | 检测方案 | ✅ |
| t_dark_rule_scheme_item | 方案规则配置项 | ✅ |
| t_dark_detect_batch | 检测批次（batch_no 自动生成、关联方案 scheme_id） | ✅ |
| t_dark_detect_task | 检测任务/文件一体化（状态、进度、结果摘要） | ✅ |
| t_dark_detect_result | 检测结果明细 | ✅ |
| t_dark_detect_cross_result | 关联分析结果（异步任务表，无审计字段） | ✅ |

**规范要点**：业务表前缀 `t_dark_`，系统表前缀 `sys_`；字符集 `utf8mb4`、引擎 `InnoDB`；状态字段 `tinyint` 并在注释中标明取值含义；唯一索引前缀 `uk_`、普通索引前缀 `idx_`；JSON 类结果使用 `text` 类型。

<br>

## 六、核心接口清单

### 批次管理 `/demo/darkdetectbatch`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页 + 条件筛选（名称、状态、时间范围） |
| GET/POST/PUT/DELETE | `/{id}`、``、``、`` | 详情 / 新增（自动生成 batch_no）/ 修改 / 批量删除（级联软删除文件） |
| POST | `/{id}/upload` | 批次上传文件（多文件，立即返回并加入检测队列） |
| POST | `/{id}/detect` | 启动批次检测 |
| GET | `/{id}/statistics` | 批次统计（文件数、通过数、违规数、失败数） |

### 任务管理 `/demo/darkdetecttask`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page`、`/batch/{batchId}` | 分页查询、按批次查询 |
| GET | `/{id}`、`/{id}/detail` | 详情、详情（含检测进度与结果摘要） |
| POST/PUT/DELETE | `/{id}/start`、``、`` | 启动单个任务检测 / 修改 / 批量软删除 |
| POST | `/{id}/retry`、`/{id}/stop` | 重试、停止检测 |
| GET | `/{id}/download` | 下载带批注的 DOCX/PDF 文档 |
| GET | `/export` | 导出 Excel |

### 检测结果 `/demo/darkdetectresult`
分页查询 `/page`、详情 `/{id}`、导出 `/export`（支持实际值 vs 期望值、严重程度、修改建议）

### 关联分析 `/demo/darkdetectcrossresult`
分页查询 `/page`、详情 `/{id}`、按批次查询 `/batch/{batchId}`、执行分析 `POST /analyze`（body: batchId）、导出 `/export`

### 规则配置 `/demo/darkruleconfig`
分页 `/page`、详情 `/{id}`、启用/禁用 `POST /{id}/toggle`、导出 `/export`（固定值规则禁止修改删除）

### 检测方案 `/demo/darkrulescheme`
分页 `/page`、详情 `/{id}`（含关联规则项）、方案列表 `/list`、设为默认 `POST /{id}/default`（保存时自动清除其他默认）

### 敏感词 `/demo/darksensitiveword`、`/demo/darksensitivecategory`
分页 `/page`、按分类查询 `/category/{categoryId}`、新增/修改/批量删除、导出 `/export`；分类删除时校验关联敏感词

### 统计看板 `/demo/statistics`
总览 `GET /overview`、批次状态分布 `GET /batchStatus`

<br>

## 七、开发进度清单

### ✅ 已完成

**基础平台（renren 基座）**
- [x] 用户/角色/菜单/部门/数据权限、字典、参数、操作日志、定时任务、云存储
- [x] XSS 防范、Hibernate Validator 校验、Swagger 文档

**业务功能**
- [x] 敏感词管理：分类 + 敏感词合并页面、模糊搜索、分类/状态筛选、批量删除、更新时间倒序、时间格式化
- [x] 检测规则配置：50+ 预置规则（版面/字体/表格/图表/标题/目录/敏感词等分类）、多种参数类型（TEXT/NUMBER/FLOAT/SELECT/MULTI_SELECT/BOOLEAN/FIXED）、启用/禁用、固定值规则保护
- [x] 检测方案管理：方案-规则组配、参数覆盖、默认方案、按分类分组展示（el-tabs）、默认值自动填充、数据库值中文映射
- [x] 批次管理：CRUD + 条件筛选、batch_no 自动生成（BATCH+时间戳）、多文件上传（立即返回）、启动检测、统计卡片、级联软删除、任务列表入口
- [x] 任务管理（文件即任务）：状态流转（排队/检测中/完成/失败）、检测进度展示、通过比例、结果摘要、重试、停止、自动刷新、带批注文档下载、按批次名称筛选
- [x] 检测结果：违规明细（位置、实际值 vs 期望值、严重程度、修改建议）、导出
- [x] 关联分析：跨文件敏感词重复分析（SENSITIVE_CROSS）、格式一致性分析（FORMAT_CONSISTENCY）、按批次执行与查询、导出
- [x] 统计看板接口（总览、批次状态分布）

**检测引擎**
- [x] DOCX 真实解析（Apache POI）：段落、字体样式、页面设置、表格、行距/缩进/对齐
- [x] PDF 真实解析（Apache PDFBox 3.0.2）：文本内容、页面元数据、字体信息
- [x] OCR 本地识别（SmartJavaAI + OnnxRuntime）：PP-OCRv5 文字检测/识别、方向分类、SLANET+ 表格识别，DJL 引擎覆盖配置
- [x] 7 大规则执行器：Layout / Font / Table / Chart / Title / Toc / Sensitive
- [x] 检测结果组装（位置信息、违规描述、修改建议）
- [x] Word 批注（XWPFComments）与 PDF 批注（PDAnnotationText）生成
- [x] 跨文件关联分析引擎（CrossFileAnalyzer）
- [x] 异步检测执行（后台线程 + 状态轮询，上传即返回）

**基础设施**
- [x] BaseEntity 审计字段抽象 + FieldMetaObjectHandler 自动填充
- [x] DarkSoftDeleteInterceptor 软删除拦截（查询过滤/删除转更新/更新保护，异步表排除）
- [x] 业务表全部软删除，唯一性校验移至 Service 层
- [x] 业务表索引完善（idx_batch_id、idx_status、idx_create_time、idx_task_id、idx_rule_code 等）
- [x] Docker 部署支持（Dockerfile、多环境配置）

### 📋 待完成

**检测能力增强（P0-P1）**
- [ ] 敏感词批量导入（Excel 导入接口 + 前端导入弹窗）
- [ ] 规则批量启用/禁用接口 `PUT /demo/darkruleconfig/batch/enable`、按分类查询接口
- [ ] PDF 报告美化导出（综合检测报告）
- [ ] 图片格式违规检测规则深化（OCR 结果与版面规则联动）

**异步架构优化（P1）**
- [ ] 正式检测队列管理器（DetectQueueManager + 消费者线程，替代当前 new Thread 方案）
- [ ] 队列监控（队列大小、处理速度统计）
- [ ] 失败任务自动重试入队

**体验优化（P2）**
- [ ] WebSocket 实时进度推送（当前为前端轮询刷新）
- [ ] 敏感词可视化（词云图）
- [ ] 跨文件敏感词可视化展示
- [ ] 批量操作优化（批量重试、批量启动检测）
- [ ] 检测进度条动画优化

**运维支持（P3）**
- [ ] 检测日志完善、异常分类与友好提示
- [ ] 性能监控（检测耗时统计）
- [ ] 数据备份策略

<br>

## 八、本地部署

### 1. 环境要求
- JDK 17+、Maven 3.6+、MySQL 8.0+、Node.js（前端构建）

### 2. 数据库初始化
- 创建数据库 `renren_security`（编码 UTF-8）
- 执行 `renren-admin/db/mysql.sql` 初始化系统数据
- 按需执行 `db/` 目录下业务脚本（business.sql、alter_*.sql、add_indexes.sql 等）

### 3. 后端启动
- 修改 `application-dev.yml` 中 MySQL 账号密码
- OCR 配置（可选，不启用则 `paddle.enabled: false`）：

```yaml
paddle:
  enabled: true
  model.path: models   # 模型根目录，本项目已内置 models/ 下全套模型
```

- 启动前系统属性（AdminApplication.main 中已配置 DJL 默认引擎为 OnnxRuntime）
- 在项目根目录执行 `mvn clean install`，运行 `AdminApplication.java`
- 后端访问：http://localhost:8080/renren-admin ，Swagger 文档：http://localhost:8080/renren-admin/doc.html

### 4. 前端启动
```bash
cd renren-ui
npm install
npm run serve     # 开发环境
npm run build     # 生产构建
```
- 前端访问：http://localhost:8001/#/ （hash 路由）
- 账号密码：admin/admin

### 5. OCR 模型说明
`models/` 目录已内置全部本地模型，DJL 引擎默认查找 `model.onnx`，各模型目录内已建立符号链接：
```
models/
├─ppocr_v5_det/          PP-OCRv5_server_det.onnx → model.onnx
├─ppocr_v5_rec/          PP-OCRv5_server_rec.onnx → model.onnx + dict.txt
├─ppocr_mobile_v2_cls/   ch_ppocr_mobile_v2.0_cls.onnx → model.onnx
└─slanet_plus/           slanet-plus.onnx → model.onnx + table_structure_dict_ch.txt
```

### 6. Docker 部署
`docker/` 目录提供 Dockerfile 与 run.sh，配置文件支持 dev/test/prod 多环境。

<br>

## 九、开发规范

项目编码规范位于 `.trae/rules/` 目录，涵盖：
- **api-standard.md**：RESTful 接口规范、Swagger 注解、统一响应、权限控制
- **architecture.md / backend-java.md / frontend-vue.md**：模块分层、命名、编码风格
- **database.md**：表设计、字段命名、索引规范
- **async-table-design.md**：异步任务表设计（无审计字段、拦截器排除）
- **ocr-config.md**：OCR 能力接入配置与常见问题
- **project-constraints.md**：硬约束与踩坑记录

## 十、相关链接

- 开发文档：https://www.renren.io/guide/security
- Gitee 仓库：https://gitee.com/renrenio/renren-security
- [人人开源](https://www.renren.io)：https://www.renren.io

<br>
