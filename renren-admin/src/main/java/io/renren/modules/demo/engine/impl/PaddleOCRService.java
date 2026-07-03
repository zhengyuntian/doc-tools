package io.renren.modules.demo.engine.impl;

import ai.djl.Device;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import io.renren.modules.demo.engine.OCRService;
import io.renren.modules.demo.engine.model.ParsedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaddleOCRService implements OCRService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaddleOCRService.class);

    @Value("${paddle.enabled:false}")
    private boolean enabled;

    @Value("${paddle.model.path:models}")
    private String modelPath;

    private ZooModel<Image, List<float[]>> detModel;
    private Predictor<Image, List<float[]>> detPredictor;
    
    private ZooModel<Image, String> recModel;
    private Predictor<Image, String> recPredictor;

    @PostConstruct
    public void init() {
        logger.info("[OCR初始化] ========== 开始初始化OCR服务 ==========");
        logger.info("[OCR初始化] OCR服务状态: {}", enabled ? "已启用" : "未启用");

        if (enabled) {
            try {
                System.setProperty("DJL_DEFAULT_ENGINE", "OnnxRuntime");
                System.setProperty("DJL_CACHE_DIR", "/tmp/djl_cache");
                logger.info("[OCR初始化] 已设置DJL引擎为OnnxRuntime，缓存目录: /tmp/djl_cache");

                logger.info("[OCR初始化] 模型路径: {}", modelPath);
                
                File modelDir = new File(modelPath);
                logger.info("[OCR初始化] 模型目录是否存在: {}", modelDir.exists());
                
                logger.info("[OCR初始化] 正在初始化文本检测模型...");
                String detModelPath = modelPath + "/ppocr_v5_det";
                logger.info("[OCR初始化] 检测模型路径: {}", detModelPath);
                
                Criteria<Image, List<float[]>> detCriteria = Criteria.builder()
                        .setTypes(Image.class, (Class<List<float[]>>) (Class<?>) List.class)
                        .optModelUrls(new File(detModelPath).toURI().toString())
                        .optTranslator(new DetTranslator())
                        .optDevice(Device.cpu())
                        .build();
                detModel = ModelZoo.loadModel(detCriteria);
                detPredictor = detModel.newPredictor();
                logger.info("[OCR初始化] 文本检测模型初始化完成");

                logger.info("[OCR初始化] 正在初始化文字识别模型...");
                String recModelPath = modelPath + "/ppocr_v5_rec";
                logger.info("[OCR初始化] 识别模型路径: {}", recModelPath);
                
                Criteria<Image, String> recCriteria = Criteria.builder()
                        .setTypes(Image.class, String.class)
                        .optModelUrls(new File(recModelPath).toURI().toString())
                        .optTranslator(new RecTranslator())
                        .optDevice(Device.cpu())
                        .build();
                recModel = ModelZoo.loadModel(recCriteria);
                recPredictor = recModel.newPredictor();
                logger.info("[OCR初始化] 文字识别模型初始化完成");

                logger.info("[OCR初始化] 表格检测将使用OCR文字检测+网格分析方案");
            } catch (Exception e) {
                logger.error("[OCR初始化] 模型初始化失败: {}", e.getMessage(), e);
                enabled = false;
            }
        }

        logger.info("[OCR初始化] ========== OCR服务初始化完成 ==========");
    }

    private Image toDJLImage(BufferedImage bufferedImage) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return ImageFactory.getInstance().fromInputStream(bais);
    }

    @Override
    public String recognizeText(ParsedImage image) throws Exception {
        logger.debug("[OCR调试] ========== 开始图片文字识别 ==========");

        if (!enabled) {
            logger.debug("[OCR调试] OCR服务未启用，跳过识别");
            return "";
        }

        if (image == null || image.getContent() == null || image.getContent().length == 0) {
            logger.debug("[OCR调试] 图片内容为空，跳过识别");
            return "";
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getContent()));
            if (bufferedImage == null) {
                logger.debug("[OCR调试] 无法读取图片内容");
                return "";
            }

            logger.debug("[OCR调试] 图片尺寸: {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());

            Image djlImage = toDJLImage(bufferedImage);
            List<float[]> boxes = detPredictor.predict(djlImage);
            
            logger.debug("[OCR调试] 检测到文字框数量: {}", boxes.size());

            StringBuilder fullText = new StringBuilder();
            for (float[] box : boxes) {
                try {
                    int x = Math.max(0, (int) box[0]);
                    int y = Math.max(0, (int) box[1]);
                    int w = Math.max(1, (int) box[2] - x);
                    int h = Math.max(1, (int) box[3] - y);
                    
                    Image cropped = djlImage.getSubImage(x, y, w, h);
                    String text = recPredictor.predict(cropped);
                    if (text != null && !text.isEmpty()) {
                        fullText.append(text).append("\n");
                    }
                } catch (Exception e) {
                    logger.warn("[OCR调试] 识别单个文字框失败: {}", e.getMessage());
                }
            }

            String result = fullText.toString().trim();
            logger.debug("[OCR调试] 识别文本长度: {} 字符", result.length());
            logger.debug("[OCR调试] ========== 图片文字识别完成 ==========");
            return result;
        } catch (Exception e) {
            logger.error("[OCR调试] 图片文字识别失败: {}", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public Map<String, Object> recognizeTable(ParsedImage image) throws Exception {
        Map<String, Object> result = new HashMap<>();

        logger.debug("[OCR调试] ========== 开始表格检测 ==========");

        if (!enabled) {
            result.put("success", false);
            result.put("message", "OCR服务未启用");
            return result;
        }

        if (image == null || image.getContent() == null || image.getContent().length == 0) {
            result.put("success", false);
            result.put("message", "图片内容为空");
            return result;
        }

        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getContent()));
            if (bufferedImage == null) {
                result.put("success", false);
                result.put("message", "无法读取图片内容");
                return result;
            }

            logger.debug("[OCR调试] 图片尺寸: {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());

            bufferedImage = resizeImageIfNeeded(bufferedImage, 960);
            logger.debug("[OCR调试] 缩放后图片尺寸: {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());

            Image djlImage = toDJLImage(bufferedImage);
            
            logger.debug("[OCR调试] 开始调用OCR文字检测分析表格...");

            long startTime = System.currentTimeMillis();
            List<float[]> boxes = detPredictor.predict(djlImage);
            long elapsedTime = System.currentTimeMillis() - startTime;

            logger.debug("[OCR调试] OCR文字检测完成，耗时: {}ms", elapsedTime);
            logger.debug("[OCR调试] 检测到文字框数量: {}", boxes.size());

            boolean isTable = isTableImage(bufferedImage, boxes);
            
            if (isTable) {
                result.put("success", true);
                result.put("message", "检测到图片中包含表格结构");
                logger.debug("[OCR调试] 检测结果: 图片包含表格结构");
            } else {
                result.put("success", false);
                result.put("message", "未检测到表格结构");
                logger.debug("[OCR调试] 检测结果: 图片不包含表格结构");
            }

            logger.debug("[OCR调试] ========== 表格检测完成 ==========");
            return result;
        } catch (Exception e) {
            logger.error("[OCR调试] 表格检测失败: {}", e.getMessage(), e);
            
            if (bufferedImage != null) {
                logger.warn("[OCR调试] OCR检测失败，使用图片线条分析作为降级方案");
                boolean isTable = detectTableByLineAnalysis(bufferedImage);
                if (isTable) {
                    result.put("success", true);
                    result.put("message", "通过图片线条分析检测到表格结构");
                } else {
                    result.put("success", false);
                    result.put("message", "未检测到表格结构");
                }
            } else {
                result.put("success", false);
                result.put("message", "OCR检测失败");
            }
            return result;
        }
    }

    private boolean isTableImage(BufferedImage image, List<float[]> boxes) {
        if (boxes == null || boxes.size() < 4) {
            return false;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        
        ImageFeatures features = extractImageFeatures(image, width, height);
        
        if (isIdCardImage(features)) {
            logger.debug("[表格检测] 图片为身份证类型，排除表格判定");
            return false;
        }
        
        if (isCertificateImage(features)) {
            logger.debug("[表格检测] 图片为证件类型，排除表格判定");
            return false;
        }
        
        logger.debug("[表格检测] 图片线条检测 - 水平线: {}, 垂直线: {}", features.hLines, features.vLines);
        
        if (features.hLines > 30 || features.vLines > 30) {
            logger.debug("[表格检测] 线条数过多({}/{}), 可能是证件纹理，排除表格判定", features.hLines, features.vLines);
            return false;
        }
        
        List<float[]> boxCenters = new ArrayList<>();
        for (float[] box : boxes) {
            float cx = (box[0] + box[2]) / 2;
            float cy = (box[1] + box[3]) / 2;
            boxCenters.add(new float[]{cx, cy});
        }
        
        int colLines = detectGridLines(boxCenters, width, height, true);
        int rowLines = detectGridLines(boxCenters, width, height, false);
        
        logger.debug("[表格检测] 文字框网格分析 - 列线条数: {}, 行线条数: {}, 文字框数量: {}", colLines, rowLines, boxes.size());
        
        double gridScore = calculateGridScore(boxCenters, width, height);
        logger.debug("[表格检测] 网格评分: {}", gridScore);
        
        boolean hasImageGrid = (features.hLines >= 3 && features.vLines >= 2) || (features.hLines >= 2 && features.vLines >= 3);
        boolean hasTextGrid = (colLines >= 2 && rowLines >= 2);
        
        if (hasImageGrid) {
            logger.debug("[表格检测] 图片线条检测到网格结构，判定为表格");
            return true;
        }
        
        if (hasTextGrid && boxes.size() >= 8) {
            logger.debug("[表格检测] 文字框网格结构明显，判定为表格");
            return true;
        }
        
        if (gridScore > 0.4 && boxes.size() >= 10) {
            logger.debug("[表格检测] 网格评分较高，判定为表格");
            return true;
        }
        
        if (features.hLines >= 2 && features.vLines >= 1 && colLines >= 2 && rowLines >= 1 && boxes.size() >= 6) {
            logger.debug("[表格检测] 综合特征符合表格，判定为表格");
            return true;
        }
        
        return false;
    }
    
    private ImageFeatures extractImageFeatures(BufferedImage image, int width, int height) {
        ImageFeatures f = new ImageFeatures();
        f.width = width;
        f.height = height;
        f.ratio = (double) width / height;
        
        int[] hCounts = new int[height];
        int[] vCounts = new int[width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF) + 
                                 0.587 * ((rgb >> 8) & 0xFF) + 
                                 0.114 * (rgb & 0xFF));
                boolean isDark = gray < 100;
                
                if (isDark) {
                    hCounts[y]++;
                    vCounts[x]++;
                }
                
                if (y < 15 || y >= height - 15) {
                    if (isDark) f.borderPixels++;
                }
                if (x < 15 || x >= width - 15) {
                    if (isDark) f.borderPixels++;
                }
            }
        }
        
        int minLineWidth = Math.max(10, width / 10);
        int minLineHeight = Math.max(10, height / 10);
        
        for (int count : hCounts) {
            if (count >= minLineWidth) f.hLines++;
        }
        for (int count : vCounts) {
            if (count >= minLineHeight) f.vLines++;
        }
        
        f.textBlocks = countTextBlocks(image, width, height);
        f.hasRoundedCorners = checkRoundedCorners(image, width, height);
        
        return f;
    }
    
    private boolean isIdCardImage(ImageFeatures f) {
        boolean isIdCardRatio = f.ratio > 1.3 && f.ratio < 1.7;
        boolean hasTextureLines = f.hLines > 50 || f.vLines > 50;
        
        boolean result = isIdCardRatio && (hasTextureLines || f.hasRoundedCorners);
        if (result) {
            logger.debug("[表格检测] 身份证特征: 宽高比={}, 水平线={}, 垂直线={}, 圆角={}", 
                    String.format("%.2f", f.ratio), f.hLines, f.vLines, f.hasRoundedCorners);
        }
        return result;
    }
    
    private boolean checkRoundedCorners(BufferedImage image, int width, int height) {
        int cornerSize = Math.min(width, height) / 10;
        int cornerDark = 0;
        
        for (int i = 0; i < cornerSize; i++) {
            for (int j = 0; j < cornerSize; j++) {
                int gray = (int) (0.299 * ((image.getRGB(j, i) >> 16) & 0xFF) + 
                                 0.587 * ((image.getRGB(j, i) >> 8) & 0xFF) + 
                                 0.114 * (image.getRGB(j, i) & 0xFF));
                if (gray < 100) cornerDark++;
                
                gray = (int) (0.299 * ((image.getRGB(width - 1 - j, i) >> 16) & 0xFF) + 
                              0.587 * ((image.getRGB(width - 1 - j, i) >> 8) & 0xFF) + 
                              0.114 * (image.getRGB(width - 1 - j, i) & 0xFF));
                if (gray < 100) cornerDark++;
                
                gray = (int) (0.299 * ((image.getRGB(j, height - 1 - i) >> 16) & 0xFF) + 
                              0.587 * ((image.getRGB(j, height - 1 - i) >> 8) & 0xFF) + 
                              0.114 * (image.getRGB(j, height - 1 - i) & 0xFF));
                if (gray < 100) cornerDark++;
                
                gray = (int) (0.299 * ((image.getRGB(width - 1 - j, height - 1 - i) >> 16) & 0xFF) + 
                              0.587 * ((image.getRGB(width - 1 - j, height - 1 - i) >> 8) & 0xFF) + 
                              0.114 * (image.getRGB(width - 1 - j, height - 1 - i) & 0xFF));
                if (gray < 100) cornerDark++;
            }
        }
        
        return cornerDark > cornerSize * cornerSize;
    }
    
    private boolean isCertificateImage(ImageFeatures f) {
        boolean hasThickBorder = f.borderPixels > (f.width + f.height) * 15 * 0.1;
        boolean hasDenseTexture = f.textBlocks > Math.max(500, f.width * f.height / 200);
        boolean hasTextureLines = f.hLines > 50 || f.vLines > 50;
        
        if (hasThickBorder && (hasDenseTexture || hasTextureLines)) {
            logger.debug("[表格检测] 证件特征: 边框像素={}, 文本块数={}, 水平线={}, 垂直线={}", 
                    f.borderPixels, f.textBlocks, f.hLines, f.vLines);
            return true;
        }
        
        if (hasTextureLines && hasDenseTexture) {
            logger.debug("[表格检测] 证件特征(无厚边框): 文本块数={}, 水平线={}, 垂直线={}", 
                    f.textBlocks, f.hLines, f.vLines);
            return true;
        }
        
        return false;
    }
    
    private int countTextBlocks(BufferedImage image, int width, int height) {
        int blocks = 0;
        boolean inBlock = false;
        int minBlockSize = 8;
        int currentBlock = 0;
        
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int gray = (int) (0.299 * ((image.getRGB(x, y) >> 16) & 0xFF) + 
                                 0.587 * ((image.getRGB(x, y) >> 8) & 0xFF) + 
                                 0.114 * (image.getRGB(x, y) & 0xFF));
                if (gray < 120) {
                    currentBlock++;
                    inBlock = true;
                } else {
                    if (inBlock && currentBlock >= minBlockSize) {
                        blocks++;
                    }
                    inBlock = false;
                    currentBlock = 0;
                }
            }
            if (inBlock && currentBlock >= minBlockSize) {
                blocks++;
            }
            inBlock = false;
            currentBlock = 0;
        }
        
        return blocks;
    }
    
    private static class ImageFeatures {
        int width, height;
        double ratio;
        int hLines, vLines;
        int borderPixels;
        int textBlocks;
        boolean hasRoundedCorners;
    }
    
    private int detectGridLines(List<float[]> centers, int width, int height, boolean isHorizontal) {
        double[] positions = new double[centers.size()];
        for (int i = 0; i < centers.size(); i++) {
            positions[i] = isHorizontal ? centers.get(i)[0] : centers.get(i)[1];
        }
        
        Arrays.sort(positions);
        
        List<Integer> clusters = new ArrayList<>();
        int currentCluster = 1;
        double tolerance = isHorizontal ? width * 0.05 : height * 0.05;
        
        for (int i = 1; i < positions.length; i++) {
            if (positions[i] - positions[i - 1] < tolerance) {
                currentCluster++;
            } else {
                if (currentCluster >= 2) {
                    clusters.add(currentCluster);
                }
                currentCluster = 1;
            }
        }
        if (currentCluster >= 2) {
            clusters.add(currentCluster);
        }
        
        return clusters.size();
    }
    
    private double calculateGridScore(List<float[]> centers, int width, int height) {
        if (centers.size() < 4) {
            return 0;
        }
        
        double[] xs = new double[centers.size()];
        double[] ys = new double[centers.size()];
        for (int i = 0; i < centers.size(); i++) {
            xs[i] = centers.get(i)[0];
            ys[i] = centers.get(i)[1];
        }
        
        Arrays.sort(xs);
        Arrays.sort(ys);
        
        double xMedianGap = calculateMedianGap(xs);
        double yMedianGap = calculateMedianGap(ys);
        
        double xScore = calculateAlignmentScore(xs, xMedianGap);
        double yScore = calculateAlignmentScore(ys, yMedianGap);
        
        return (xScore + yScore) / 2;
    }
    
    private double calculateMedianGap(double[] sortedPositions) {
        List<Double> gaps = new ArrayList<>();
        for (int i = 1; i < sortedPositions.length; i++) {
            gaps.add(sortedPositions[i] - sortedPositions[i - 1]);
        }
        Collections.sort(gaps);
        return gaps.get(gaps.size() / 2);
    }
    
    private double calculateAlignmentScore(double[] sortedPositions, double medianGap) {
        if (medianGap < 1) {
            return 0;
        }
        
        int alignedCount = 0;
        double tolerance = medianGap * 0.3;
        
        for (int i = 0; i < sortedPositions.length; i++) {
            for (int j = i + 1; j < sortedPositions.length; j++) {
                double diff = Math.abs(sortedPositions[i] - sortedPositions[j]);
                double remainder = diff % medianGap;
                if (remainder < tolerance || medianGap - remainder < tolerance) {
                    alignedCount++;
                }
            }
        }
        
        int totalPairs = sortedPositions.length * (sortedPositions.length - 1) / 2;
        return totalPairs > 0 ? (double) alignedCount / totalPairs : 0;
    }
    
    private int countLines(int[] counts, int threshold) {
        int lines = 0;
        boolean inLine = false;
        int minLineWidth = 10;
        int currentWidth = 0;
        
        for (int count : counts) {
            if (count >= threshold) {
                currentWidth++;
                inLine = true;
            } else {
                if (inLine && currentWidth >= minLineWidth) {
                    lines++;
                }
                inLine = false;
                currentWidth = 0;
            }
        }
        if (inLine && currentWidth >= minLineWidth) {
            lines++;
        }
        return lines;
    }

    private boolean detectTableByLineAnalysis(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width < 50 || height < 50) {
            return false;
        }
        
        int grayWidth = Math.max(1, width / 200);
        int grayHeight = Math.max(1, height / 200);
        
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                grayImage.setRGB(x, y, gray << 16 | gray << 8 | gray);
            }
        }
        
        int horizontalLines = 0;
        int verticalLines = 0;
        int lineThreshold = 20;
        int minLineLength = Math.max(width, height) / 5;
        
        for (int y = 0; y < height; y += grayHeight) {
            int runLength = 0;
            for (int x = 0; x < width; x += grayWidth) {
                int gray = (grayImage.getRGB(x, y) & 0xFF);
                if (gray < lineThreshold) {
                    runLength++;
                } else {
                    if (runLength * grayWidth >= minLineLength) {
                        horizontalLines++;
                    }
                    runLength = 0;
                }
            }
            if (runLength * grayWidth >= minLineLength) {
                horizontalLines++;
            }
        }
        
        for (int x = 0; x < width; x += grayWidth) {
            int runLength = 0;
            for (int y = 0; y < height; y += grayHeight) {
                int gray = (grayImage.getRGB(x, y) & 0xFF);
                if (gray < lineThreshold) {
                    runLength++;
                } else {
                    if (runLength * grayHeight >= minLineLength) {
                        verticalLines++;
                    }
                    runLength = 0;
                }
            }
            if (runLength * grayHeight >= minLineLength) {
                verticalLines++;
            }
        }
        
        logger.debug("[表格检测] 水平线条数: {}, 垂直线条数: {}", horizontalLines, verticalLines);
        
        return horizontalLines >= 3 && verticalLines >= 3;
    }

    private BufferedImage resizeImageIfNeeded(BufferedImage image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (Math.max(width, height) <= maxSize) {
            return image;
        }
        
        float ratio = (float) maxSize / (float) Math.max(width, height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);
        
        newWidth = Math.round((float) newWidth / 32f) * 32;
        newHeight = Math.round((float) newHeight / 32f) * 32;
        
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return resized;
    }

    public static class DetTranslator implements Translator<Image, List<float[]>> {
        private static final int LIMIT_SIDE_LEN = 960;
        
        @Override
        public NDList processInput(TranslatorContext ctx, Image input) {
            int h = input.getHeight();
            int w = input.getWidth();
            
            float ratio = 1.0f;
            if (Math.max(h, w) > LIMIT_SIDE_LEN) {
                ratio = (float) LIMIT_SIDE_LEN / (float) Math.max(h, w);
            }
            
            int resize_h = (int) (h * ratio);
            int resize_w = (int) (w * ratio);
            
            resize_h = Math.round((float) resize_h / 32f) * 32;
            resize_w = Math.round((float) resize_w / 32f) * 32;
            
            Image resizedImage = input.resize(resize_w, resize_h, true);
            
            BufferedImage bufferedImage = null;
            Object wrapped = resizedImage.getWrappedImage();
            if (wrapped instanceof BufferedImage) {
                bufferedImage = (BufferedImage) wrapped;
            }
            if (bufferedImage == null) {
                bufferedImage = new BufferedImage(resize_w, resize_h, BufferedImage.TYPE_3BYTE_BGR);
            }
            
            int channels = 3;
            float[] data = new float[channels * resize_h * resize_w];
            float[] mean = {0.485f, 0.456f, 0.406f};
            float[] std = {0.229f, 0.224f, 0.225f};
            
            for (int c = 0; c < channels; c++) {
                for (int y = 0; y < resize_h; y++) {
                    for (int x = 0; x < resize_w; x++) {
                        int pixel = bufferedImage.getRGB(x, y);
                        int b = (pixel) & 0xFF;
                        int g = (pixel >> 8) & 0xFF;
                        int r = (pixel >> 16) & 0xFF;
                        
                        float value;
                        if (c == 0) {
                            value = (r / 255.0f - mean[0]) / std[0];
                        } else if (c == 1) {
                            value = (g / 255.0f - mean[1]) / std[1];
                        } else {
                            value = (b / 255.0f - mean[2]) / std[2];
                        }
                        data[c * resize_h * resize_w + y * resize_w + x] = value;
                    }
                }
            }
            
            NDManager manager = ctx.getNDManager();
            NDArray img = manager.create(data, new Shape(1, channels, resize_h, resize_w));
            
            return new NDList(img);
        }
        
        @Override
        public List<float[]> processOutput(TranslatorContext ctx, NDList list) {
            List<float[]> result = new ArrayList<>();
            NDArray pred = list.get(0);
            
            Shape predShape = pred.getShape();
            int dims = predShape.dimension();
            
            int rows = 0, cols = 0;
            long totalElements = 1;
            for (int i = 0; i < dims; i++) {
                totalElements *= predShape.get(i);
            }
            
            float[] rawData = pred.toFloatArray();
            float[] allData = new float[rawData.length];
            for (int i = 0; i < rawData.length; i++) {
                allData[i] = rawData[i] > 0.3f ? 1.0f : 0.0f;
            }
            
            if (dims == 2) {
                rows = (int) predShape.get(0);
                cols = (int) predShape.get(1);
            } else if (dims == 3) {
                int nonOneDimCount = 0;
                int[] nonOneDims = new int[2];
                for (int i = 0; i < dims; i++) {
                    if (predShape.get(i) != 1) {
                        nonOneDims[nonOneDimCount++] = i;
                    }
                }
                if (nonOneDimCount == 2) {
                    rows = (int) predShape.get(nonOneDims[0]);
                    cols = (int) predShape.get(nonOneDims[1]);
                } else {
                    rows = (int) predShape.get(0);
                    cols = (int) predShape.get(1);
                }
            } else if (dims == 4) {
                int nonOneDimCount = 0;
                int[] nonOneDims = new int[2];
                for (int i = 0; i < dims; i++) {
                    if (predShape.get(i) != 1) {
                        nonOneDims[nonOneDimCount++] = i;
                    }
                }
                if (nonOneDimCount == 2) {
                    rows = (int) predShape.get(nonOneDims[0]);
                    cols = (int) predShape.get(nonOneDims[1]);
                } else {
                    long batchSize = predShape.get(0);
                    long channels = predShape.get(1);
                    rows = (int) predShape.get(2);
                    cols = (int) predShape.get(3);
                    if (batchSize == 1 && channels == 1) {
                        // shape is [1, 1, H, W]
                    } else if (batchSize == 1) {
                        // shape is [1, C, H, W], flatten C into rows
                        rows = (int) (channels * rows);
                    }
                }
            } else {
                rows = 1;
                cols = (int) totalElements;
            }
            
            float[] data = new float[rows * cols];
            if (allData.length >= rows * cols) {
                System.arraycopy(allData, 0, data, 0, rows * cols);
            } else {
                System.arraycopy(allData, 0, data, 0, allData.length);
            }
            
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (data[y * cols + x] > 0.5f) {
                        float[] box = new float[4];
                        box[0] = Math.max(0, x - 5);
                        box[1] = Math.max(0, y - 5);
                        box[2] = Math.min(cols, x + 5);
                        box[3] = Math.min(rows, y + 5);
                        result.add(box);
                    }
                }
            }
            
            return result;
        }
        
        @Override
        public Batchifier getBatchifier() {
            return null;
        }
    }

    public static class RecTranslator implements Translator<Image, String> {
        private static final int MAX_SIDE_LEN = 320;
        private static final int HEIGHT = 48;
        
        @Override
        public NDList processInput(TranslatorContext ctx, Image input) {
            int h = input.getHeight();
            int w = input.getWidth();
            
            float ratio = (float) HEIGHT / (float) h;
            int resize_w = (int) (w * ratio);
            
            if (resize_w > MAX_SIDE_LEN) {
                resize_w = MAX_SIDE_LEN;
            }
            
            Image resizedImage = input.resize(resize_w, HEIGHT, true);
            
            BufferedImage bufferedImage = null;
            Object wrapped = resizedImage.getWrappedImage();
            if (wrapped instanceof BufferedImage) {
                bufferedImage = (BufferedImage) wrapped;
            }
            if (bufferedImage == null) {
                bufferedImage = new BufferedImage(resize_w, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
            }
            
            int channels = 3;
            float[] data = new float[channels * HEIGHT * resize_w];
            float[] mean = {0.5f, 0.5f, 0.5f};
            float[] std = {0.5f, 0.5f, 0.5f};
            
            for (int c = 0; c < channels; c++) {
                for (int y = 0; y < HEIGHT; y++) {
                    for (int x = 0; x < resize_w; x++) {
                        int pixel = bufferedImage.getRGB(x, y);
                        int b = (pixel) & 0xFF;
                        int g = (pixel >> 8) & 0xFF;
                        int r = (pixel >> 16) & 0xFF;
                        
                        float value;
                        if (c == 0) {
                            value = (r / 255.0f - mean[0]) / std[0];
                        } else if (c == 1) {
                            value = (g / 255.0f - mean[1]) / std[1];
                        } else {
                            value = (b / 255.0f - mean[2]) / std[2];
                        }
                        data[c * HEIGHT * resize_w + y * resize_w + x] = value;
                    }
                }
            }
            
            NDManager manager = ctx.getNDManager();
            NDArray img = manager.create(data, new Shape(1, channels, HEIGHT, resize_w));
            
            return new NDList(img);
        }
        
        @Override
        public String processOutput(TranslatorContext ctx, NDList list) {
            NDArray pred = list.get(0);
            Shape shape = pred.getShape();
            int batchSize = (int) shape.get(0);
            int seqLen = (int) shape.get(1);
            int vocabSize = (int) shape.get(2);
            
            float[] data = pred.toFloatArray();
            
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < seqLen; i++) {
                int maxIdx = 0;
                float maxVal = data[0 * seqLen * vocabSize + i * vocabSize + 0];
                for (int j = 1; j < vocabSize; j++) {
                    float val = data[0 * seqLen * vocabSize + i * vocabSize + j];
                    if (val > maxVal) {
                        maxVal = val;
                        maxIdx = j;
                    }
                }
                if (maxIdx > 0 && maxIdx < vocabSize - 1) {
                    sb.append((char) maxIdx);
                }
            }
            
            return sb.toString();
        }
        
        @Override
        public Batchifier getBatchifier() {
            return null;
        }
    }
}
