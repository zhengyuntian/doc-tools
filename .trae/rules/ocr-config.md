# OCR能力接入配置规则

## 概述

本项目通过集成 **SmartJavaAI** 框架实现本地OCR能力，支持：
- 图片文字识别（PP-OCRv5）
- 图片表格结构识别（SLANET）

所有模型均为本地部署，无需外部服务。

---

## 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| SmartJavaAI | 1.0.23 | OCR框架，封装DJL和PaddleOCR模型 |
| DJL | 0.34.0 | Deep Java Library，深度学习推理引擎 |
| OnnxRuntime | 1.20.0 | ONNX模型推理引擎（CPU） |
| PP-OCRv5 | - | 百度PaddleOCR文字识别模型 |
| SLANET+ | - | 表格结构识别模型 |

---

## 依赖配置

### pom.xml 依赖

```xml
<properties>
    <smartjavaai.version>1.0.23</smartjavaai.version>
</properties>

<dependencies>
    <dependency>
        <groupId>cn.smartjavaai</groupId>
        <artifactId>smartjavaai-ocr</artifactId>
        <version>${smartjavaai.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.poi</groupId>
                <artifactId>poi</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.poi</groupId>
                <artifactId>poi-ooxml</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.poi</groupId>
                <artifactId>poi-ooxml-schemas</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

**说明**：排除POI依赖是为了避免与项目中已有的POI 5.2.5版本冲突。

---

## 配置文件

### application.yml

```yaml
paddle:
  enabled: true                                    # 是否启用OCR服务
  model.path: /Users/admin/IdeaProjects/docTools/models  # 模型文件根目录
```

### 系统属性配置（必须）

在 `AdminApplication.java` 的 `main` 方法中设置：

```java
public static void main(String[] args) {
    // 设置DJL默认引擎为OnnxRuntime（关键配置）
    System.setProperty("ai.djl.default_engine", "OnnxRuntime");
    System.setProperty("DJL_DEFAULT_ENGINE", "OnnxRuntime");
    // 设置缓存目录（避免权限问题）
    System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
    System.setProperty("DJL_PYTORCH_HOME", "/tmp/pytorch_cache");
    System.setProperty("PYTORCH_HOME", "/tmp/pytorch_cache");
    // 禁用OnnxRuntime替代引擎（避免加载PyTorch）
    System.setProperty("ai.djl.onnx.disable_alternative", "true");
    SpringApplication.run(AdminApplication.class, args);
}
```

---

## 模型文件结构

模型文件需放置在配置的 `model.path` 目录下：

```
models/
├── ppocr_v5_det/                    # 文本检测模型
│   ├── PP-OCRv5_server_det.onnx
│   └── model.onnx -> PP-OCRv5_server_det.onnx  # 符号链接
├── ppocr_v5_rec/                    # 文字识别模型
│   ├── PP-OCRv5_server_rec.onnx
│   ├── model.onnx -> PP-OCRv5_server_rec.onnx  # 符号链接
│   └── dict.txt                     # 字典文件（必须）
├── ppocr_mobile_v2_cls/             # 方向分类模型
│   ├── ch_ppocr_mobile_v2.0_cls.onnx
│   └── model.onnx -> ch_ppocr_mobile_v2.0_cls.onnx  # 符号链接
└── slanet_plus/                     # 表格识别模型
    ├── slanet-plus.onnx
    ├── model.onnx -> slanet-plus.onnx  # 符号链接
    └── table_structure_dict_ch.txt     # 表格字典文件（必须）
```

### 模型下载

模型文件需自行下载并放置到指定目录：
- PP-OCRv5 Server模型（检测+识别）：适用于服务器端高精度识别
- PP-OCR Mobile V2 方向分类模型：用于文字方向检测（0/90/180/270度）
- SLANET+ 表格识别模型：用于图片中表格结构识别

### 符号链接创建

DJL引擎默认查找 `model.onnx` 文件，需为每个模型创建符号链接：

```bash
cd models/ppocr_v5_det && ln -sf PP-OCRv5_server_det.onnx model.onnx
cd models/ppocr_v5_rec && ln -sf PP-OCRv5_server_rec.onnx model.onnx
cd models/ppocr_mobile_v2_cls && ln -sf ch_ppocr_mobile_v2.0_cls.onnx model.onnx
cd models/slanet_plus && ln -sf slanet-plus.onnx model.onnx
```

---

## 核心实现类

### PaddleOCRService

**路径**：`renren-admin/src/main/java/io/renren/modules/demo/engine/impl/PaddleOCRService.java`

**功能**：实现 `OCRService` 接口，提供文字识别和表格识别能力

**关键配置**：

```java
// 文本检测模型配置
OcrDetModelConfig detConfig = new OcrDetModelConfig();
detConfig.setModelEnum(CommonDetModelEnum.PP_OCR_V5_SERVER_DET_MODEL);
detConfig.setDetModelPath(modelPath + "/ppocr_v5_det");

// 方向分类模型配置
DirectionModelConfig dirConfig = new DirectionModelConfig();
dirConfig.setModelEnum(DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS);
dirConfig.setModelPath(modelPath + "/ppocr_mobile_v2_cls");

// 文字识别模型配置
OcrRecModelConfig recConfig = new OcrRecModelConfig();
recConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_SERVER_REC_MODEL);
recConfig.setRecModelPath(modelPath + "/ppocr_v5_rec");
recConfig.setTextDetModel(detModel);
recConfig.setDirectionModel(dirModel);

// 表格识别模型配置（必须设置modelEnum）
TableStructureConfig tableConfig = new TableStructureConfig();
tableConfig.setModelEnum(TableStructureModelEnum.SLANET_PLUS);
tableConfig.setModelPath(modelPath + "/slanet_plus");
```

**API方法**：

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `recognizeText(ParsedImage image)` | 识别图片中的文字 | String（识别文本） |
| `recognizeTable(ParsedImage image)` | 识别图片中的表格结构 | Map（包含HTML表格） |

---

## SmartJavaAI Config类覆盖

### 问题背景

SmartJavaAI的 `cn.smartjavaai.common.config.Config` 类在静态代码块中硬编码设置 `ai.djl.default_engine` 为 "PyTorch"，会覆盖我们在启动时设置的 OnnxRuntime 配置。

### 解决方案

在项目中创建同名类覆盖：

**路径**：`renren-admin/src/main/java/cn/smartjavaai/common/config/Config.java`

**关键修改**：

```java
static {
    createCachePath();
    if(StringUtils.isNotBlank(cachePath)){
        System.setProperty("DJL_CACHE_DIR", cachePath);
    }
    // 尊重已有的系统属性设置，不强制覆盖为PyTorch
    String defaultEngine = System.getProperty("ai.djl.default_engine");
    if(StringUtils.isBlank(defaultEngine)){
        defaultEngine = System.getProperty("DJL_DEFAULT_ENGINE");
    }
    if(StringUtils.isBlank(defaultEngine)){
        defaultEngine = "PyTorch"; // 默认值
    }
    System.setProperty("ai.djl.default_engine", defaultEngine);
}
```

**原理**：Java类加载机制中，项目源码中的类会优先于依赖jar包中的类加载。

---

## OCR初始化流程

```
1. 应用启动（AdminApplication.main）
   └── 设置系统属性 ai.djl.default_engine=OnnxRuntime

2. Spring容器初始化
   └── PaddleOCRService @PostConstruct 执行

3. 模型加载顺序
   ├── 文本检测模型（ppocr_v5_det）
   ├── 方向分类模型（ppocr_mobile_v2_cls）
   ├── 文字识别模型（ppocr_v5_rec）
   └── 表格识别模型（slanet_plus）

4. 初始化成功日志
   └── [OCR初始化] ========== OCR服务初始化完成 ==========
```

### 成功初始化日志示例

```
[DJL配置] ai.djl.default_engine = OnnxRuntime
[OCR初始化] ========== 开始初始化OCR服务 ==========
[OCR初始化] OCR服务状态: 已启用
[OCR初始化] 文本检测模型初始化完成
[OCR初始化] 方向分类模型初始化完成
[OCR初始化] 文字识别模型初始化完成
[OCR初始化] 表格模型枚举: SLANET_PLUS
[OCR初始化] 表格识别模型初始化完成
[OCR初始化] ========== OCR服务初始化完成 ==========
```

---

## 常见问题与解决方案

### 1. 模型初始化失败：`No PyTorch native library matches your operating system`

**原因**：SmartJavaAI的Config类强制设置引擎为PyTorch

**解决方案**：创建覆盖类 `cn.smartjavaai.common.config.Config`，尊重已有的系统属性

### 2. 模型初始化失败：`.onnx file not found in: xxx`

**原因**：DJL引擎默认查找 `model.onnx` 文件

**解决方案**：为每个模型目录创建 `model.onnx` 符号链接

### 3. 表格识别失败：`未配置OCR模型`

**原因**：`TableStructureConfig` 未设置 `modelEnum`

**解决方案**：设置 `tableConfig.setModelEnum(TableStructureModelEnum.SLANET_PLUS)`

### 4. 引擎初始化警告：`CUDA is not supported`

**原因**：OnnxRuntime未检测到CUDA GPU

**解决方案**：正常现象，CPU环境下自动回退到CPU推理

### 5. 权限问题：`Operation not permitted`

**原因**：DJL缓存目录权限不足

**解决方案**：设置 `DJL_CACHE_DIR=/tmp/djl_cache`

---

## 模型版本建议

| 模型类型 | 推荐模型 | 适用场景 |
|---------|---------|---------|
| 文本检测 | PP-OCRv5 Server | 服务器端，高精度 |
| 文本识别 | PP-OCRv5 Server | 服务器端，高精度 |
| 方向分类 | PP-OCR Mobile V2 | 轻量级，通用 |
| 表格识别 | SLANET+ | 通用表格结构识别 |

---

## CPU环境注意事项

1. **推理速度**：OCR识别速度与图片大小、CPU核心数相关，建议使用4核以上CPU
2. **内存占用**：加载所有模型约占用1-2GB内存
3. **首次加载**：首次启动时OnnxRuntime会编译优化模型，耗时较长（约30-60秒）
4. **并发处理**：表格识别模型使用Predictor池，默认池大小为CPU核心数

---

## 扩展说明

### 支持的图片格式

- PNG、JPEG、JPG、BMP、TIFF

### 识别能力

- **文字识别**：支持中文、英文、数字及常见符号
- **表格识别**：输出HTML格式表格，包含单元格合并信息

### 与规则引擎的集成

OCR识别结果可用于以下检测规则：
- 图片中的敏感词检测
- 图片中的格式违规检测
- 表格图片识别（禁止以图片格式插入表格）