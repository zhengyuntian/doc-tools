package io.renren.modules.demo.util;

import io.renren.modules.demo.entity.DarkDetectResultEntity;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPopup;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PdfAnnotationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfAnnotationUtils.class);

    public static File addAnnotations(String filePath, List<DarkDetectResultEntity> results) throws Exception {
        File originalFile = new File(filePath);
        if (!originalFile.exists()) {
            return originalFile;
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        File tempFile = new File(System.getProperty("java.io.tmpdir"), "annotated_" + uuid + ".pdf");
        
        if (tempFile.exists()) {
            tempFile.delete();
        }

        try (PDDocument document = Loader.loadPDF(originalFile)) {
            if (results == null || results.isEmpty()) {
                document.save(tempFile);
                return tempFile;
            }

            Map<Integer, List<DarkDetectResultEntity>> resultsByPage = new HashMap<>();
            for (DarkDetectResultEntity result : results) {
                if (result.getIsPass() != null && result.getIsPass() == 0) {
                    int pageIndex = result.getPageNo() != null && result.getPageNo() > 0 ? result.getPageNo() - 1 : 0;
                    resultsByPage.computeIfAbsent(pageIndex, k -> new java.util.ArrayList<>()).add(result);
                }
            }

            for (Map.Entry<Integer, List<DarkDetectResultEntity>> entry : resultsByPage.entrySet()) {
                int pageIndex = entry.getKey();
                if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
                    PDPage page = document.getPage(pageIndex);
                    List<DarkDetectResultEntity> pageResults = entry.getValue();
                    
                    List<TextPositionInfo> allTextPositions = extractAllTextPositions(document, page);
                    List<ImagePositionInfo> allImagePositions = extractImagePositions(page);
                    
                    addAnnotationsToPage(page, pageResults, pageIndex + 1, allTextPositions, allImagePositions);
                }
            }

            document.save(tempFile);
        }

        return tempFile;
    }

    private static List<TextPositionInfo> extractAllTextPositions(PDDocument document, PDPage page) {
        List<TextPositionInfo> positions = new ArrayList<>();
        
        try {
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (textPositions != null && !textPositions.isEmpty()) {
                        TextPosition first = textPositions.get(0);
                        TextPosition last = textPositions.get(textPositions.size() - 1);
                        
                        positions.add(new TextPositionInfo(
                            first.getX(),
                            first.getY(),
                            last.getX() + last.getWidth(),
                            first.getY() - first.getHeight(),
                            text.trim()
                        ));
                    }
                    super.writeString(text, textPositions);
                }
            };
            
            stripper.setStartPage(document.getPages().indexOf(page) + 1);
            stripper.setEndPage(document.getPages().indexOf(page) + 1);
            stripper.getText(document);
        } catch (IOException e) {
            logger.error("[PDF标注] 提取文本位置失败: {}", e.getMessage());
        }
        
        return positions;
    }

    private static List<ImagePositionInfo> extractImagePositions(PDPage page) {
        List<ImagePositionInfo> positions = new ArrayList<>();
        
        ImagePositionExtractor extractor = new ImagePositionExtractor();
        try {
            extractor.processPage(page);
            positions.addAll(extractor.getImagePositions());
            logger.debug("[PDF图片提取] 通过内容流解析提取到 {} 张图片", positions.size());
        } catch (IOException e) {
            logger.error("[PDF标注] 使用内容流解析提取图片位置失败: {}", e.getMessage());
        }
        
        if (positions.isEmpty()) {
            try {
                PDResources resources = page.getResources();
                if (resources != null) {
                    Iterable<org.apache.pdfbox.cos.COSName> xObjectNames = resources.getXObjectNames();
                    if (xObjectNames != null) {
                        int imageCount = 0;
                        for (org.apache.pdfbox.cos.COSName name : xObjectNames) {
                            Object xobject = resources.getXObject(name);
                            if (xobject instanceof PDImageXObject) {
                                imageCount++;
                            }
                        }
                        logger.debug("[PDF图片提取] 页面资源中有 {} 张图片，但内容流解析未找到位置", imageCount);
                        
                        PDRectangle cropBox = page.getCropBox();
                        float pageWidth = cropBox.getWidth();
                        float pageHeight = cropBox.getHeight();
                        float margin = 50;
                        
                        int index = 0;
                        for (org.apache.pdfbox.cos.COSName name : xObjectNames) {
                            Object xobject = resources.getXObject(name);
                            if (xobject instanceof PDImageXObject) {
                                PDImageXObject image = (PDImageXObject) xobject;
                                
                                float imgWidth = Math.min(image.getWidth(), pageWidth - margin * 2);
                                float imgHeight = Math.min(image.getHeight(), pageHeight - margin * 2);
                                
                                float x = margin + index * 50;
                                float y = margin + index * 100;
                                
                                if (x + imgWidth > pageWidth - margin) {
                                    x = (pageWidth - imgWidth) / 2;
                                }
                                if (y + imgHeight > pageHeight - margin) {
                                    y = (pageHeight - imgHeight) / 2;
                                }
                                
                                positions.add(new ImagePositionInfo(x, y, x + imgWidth, y + imgHeight));
                                
                                logger.debug("[PDF图片提取] 估算图片位置: x={}, y={}, width={}, height={}", x, y, imgWidth, imgHeight);
                                index++;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                logger.error("[PDF标注] 提取图片位置失败: {}", ex.getMessage());
            }
        }
        
        return positions;
    }

    private static void addAnnotationsToPage(PDPage page, List<DarkDetectResultEntity> results, int pageNo,
                                              List<TextPositionInfo> allTextPositions, List<ImagePositionInfo> allImagePositions) throws IOException {
        Set<Integer> usedImageIndices = new HashSet<>();
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight();
        float margin = 20;
        
        logger.debug("[PDF标注] 页面 {}: 检测结果数={}, 文本位置数={}, 图片位置数={}", 
            pageNo, results.size(), allTextPositions.size(), allImagePositions.size());
        
        for (int i = 0; i < results.size(); i++) {
            DarkDetectResultEntity result = results.get(i);
            boolean isImageResult = "image".equals(result.getRuleCategory()) 
                || result.getRemark() != null && result.getRemark().contains("图片")
                || result.getActualValue() != null && result.getActualValue().contains("图片");
            
            float annotX = margin + i * 30;
            float annotY = pageHeight - margin - i * 30;
            
            if (isImageResult) {
                ImagePositionInfo matchedImage = findBestImageMatch(result, allImagePositions, usedImageIndices);
                if (matchedImage != null) {
                    addImageHighlightAnnotation(page, result, matchedImage);
                    annotX = matchedImage.x1;
                    annotY = matchedImage.y1;
                } else {
                    logger.warn("[PDF标注] 页面 {}: 图片结果 {} 未找到匹配的图片位置", pageNo, i);
                }
            } else {
                TextPositionInfo matchedPos = addTextHighlightAnnotation(page, result, allTextPositions);
                if (matchedPos != null) {
                    annotX = matchedPos.x1;
                    annotY = matchedPos.y1;
                } else {
                    logger.warn("[PDF标注] 页面 {}: 文本结果 {} 未找到匹配的文本位置", pageNo, i);
                }
            }
            
            addCommentAnnotation(page, result, annotX, annotY, pageWidth, pageHeight, margin);
        }
    }
    
    private static ImagePositionInfo findBestImageMatch(DarkDetectResultEntity result, List<ImagePositionInfo> allImagePositions, Set<Integer> usedImageIndices) {
        Integer paragraphIndex = result.getParagraphIndex();
        if (paragraphIndex != null && paragraphIndex >= 0 && paragraphIndex < allImagePositions.size()) {
            if (!usedImageIndices.contains(paragraphIndex)) {
                usedImageIndices.add(paragraphIndex);
                return allImagePositions.get(paragraphIndex);
            }
        }
        
        for (int i = 0; i < allImagePositions.size(); i++) {
            if (!usedImageIndices.contains(i)) {
                usedImageIndices.add(i);
                return allImagePositions.get(i);
            }
        }
        
        return null;
    }

    private static void addImageHighlightAnnotation(PDPage page, DarkDetectResultEntity result, ImagePositionInfo imagePos) throws IOException {
        String contents = buildAnnotationContent(result);
        
        float[] quadPoints = {
            imagePos.x1, imagePos.y1, imagePos.x2, imagePos.y1,
            imagePos.x1, imagePos.y2, imagePos.x2, imagePos.y2
        };
        
        PDAnnotationHighlight highlight = new PDAnnotationHighlight();
        highlight.setQuadPoints(quadPoints);
        
        PDColor red = new PDColor(new float[]{1.0f, 0.5f, 0.5f}, PDDeviceRGB.INSTANCE);
        highlight.setColor(red);
        highlight.setContents(contents);
        
        page.getAnnotations().add(highlight);
        
        logger.debug("[PDF图片标注] 添加高亮: x={}, y={}, width={}, height={}", imagePos.x1, imagePos.y1, (imagePos.x2 - imagePos.x1), (imagePos.y1 - imagePos.y2));
    }

    private static TextPositionInfo addTextHighlightAnnotation(PDPage page, DarkDetectResultEntity result, List<TextPositionInfo> allTextPositions) throws IOException {
        String searchText = null;
        
        if (result.getActualValue() != null && !result.getActualValue().isEmpty()) {
            searchText = result.getActualValue();
        } else if (result.getRemark() != null && !result.getRemark().isEmpty()) {
            searchText = result.getRemark();
        }
        
        if (searchText != null && !searchText.isEmpty()) {
            TextPositionInfo matchedPos = findBestTextMatch(searchText, allTextPositions);
            if (matchedPos != null) {
                addHighlightAnnotation(page, result, matchedPos);
                logger.debug("[PDF文本高亮] 通过文本匹配找到位置: {}", matchedPos.text.substring(0, Math.min(matchedPos.text.length(), 30)) + "...");
                return matchedPos;
            } else {
                logger.warn("[PDF文本高亮] 文本匹配失败: searchText={}", searchText.substring(0, Math.min(searchText.length(), 50)) + "...");
            }
        }
        
        Integer paragraphIndex = result.getParagraphIndex();
        if (paragraphIndex != null && paragraphIndex >= 0 && paragraphIndex < allTextPositions.size()) {
            TextPositionInfo pos = allTextPositions.get(paragraphIndex);
            addHighlightAnnotation(page, result, pos);
            logger.debug("[PDF文本高亮] 通过段落索引找到位置: index={}", paragraphIndex);
            return pos;
        }
        
        if (paragraphIndex != null && paragraphIndex >= allTextPositions.size()) {
            logger.warn("[PDF文本高亮] 段落索引 {} 超出范围，总文本数 {}", paragraphIndex, allTextPositions.size());
        }
        
        if (!allTextPositions.isEmpty()) {
            TextPositionInfo pos = allTextPositions.get(0);
            addHighlightAnnotation(page, result, pos);
            logger.debug("[PDF文本高亮] 使用页面第一个文本位置");
            return pos;
        }
        
        logger.warn("[PDF文本高亮] 页面没有文本位置");
        return null;
    }
    
    private static TextPositionInfo findBestTextMatch(String searchText, List<TextPositionInfo> allTextPositions) {
        searchText = searchText.trim();
        
        for (TextPositionInfo pos : allTextPositions) {
            String text = pos.text.trim();
            if (text.contains(searchText) || searchText.contains(text)) {
                return pos;
            }
        }
        
        String[] searchWords = searchText.split("[，,。.\\s]+");
        for (TextPositionInfo pos : allTextPositions) {
            String text = pos.text.trim();
            int matchCount = 0;
            for (String word : searchWords) {
                if (word.length() > 2 && text.contains(word)) {
                    matchCount++;
                }
            }
            if (matchCount >= searchWords.length / 2) {
                return pos;
            }
        }
        
        for (TextPositionInfo pos : allTextPositions) {
            if (pos.text.length() > 0) {
                return pos;
            }
        }
        
        return null;
    }
    
    private static void addHighlightAnnotation(PDPage page, DarkDetectResultEntity result, TextPositionInfo pos) throws IOException {
        String contents = buildAnnotationContent(result);
        
        PDAnnotationHighlight highlight = new PDAnnotationHighlight();
        
        float[] quadPoints = {
            pos.x1, pos.y1, pos.x2, pos.y1, pos.x1, pos.y2, pos.x2, pos.y2
        };
        highlight.setQuadPoints(quadPoints);
        
        PDColor yellow = new PDColor(new float[]{1.0f, 1.0f, 0.0f}, PDDeviceRGB.INSTANCE);
        highlight.setColor(yellow);
        highlight.setContents(contents);
        
        page.getAnnotations().add(highlight);
    }

    private static final float POPUP_WIDTH = 280;
    private static final float POPUP_HEIGHT = 80;
    private static final float ANNOT_SIZE = 18;
    
    private static void addCommentAnnotation(PDPage page, DarkDetectResultEntity result, float targetX, float targetY, 
                                              float pageWidth, float pageHeight, float margin) throws IOException {
        String contents = buildAnnotationContent(result);
        
        float annotX = targetX;
        float annotY = targetY - 10;
        
        annotX = Math.max(margin, Math.min(annotX, pageWidth - margin - ANNOT_SIZE));
        annotY = Math.max(margin + ANNOT_SIZE, Math.min(annotY, pageHeight - margin - ANNOT_SIZE));
        
        PDAnnotationText annotation = new PDAnnotationText();
        annotation.setRectangle(new PDRectangle(annotX, annotY, ANNOT_SIZE, ANNOT_SIZE));
        
        PDColor yellow = new PDColor(new float[]{1.0f, 1.0f, 0.8f}, PDDeviceRGB.INSTANCE);
        annotation.setColor(yellow);
        annotation.setContents(contents);
        annotation.setLocked(false);
        annotation.setPrinted(true);
        annotation.setName("Comment");
        
        float popupX = annotX + ANNOT_SIZE + 10;
        float popupY = annotY - POPUP_HEIGHT / 2;
        
        if (popupX + POPUP_WIDTH > pageWidth - margin) {
            popupX = annotX - POPUP_WIDTH - 10;
        }
        if (popupY - POPUP_HEIGHT < margin) {
            popupY = annotY + ANNOT_SIZE + 10;
        }
        if (popupY + POPUP_HEIGHT > pageHeight - margin) {
            popupY = pageHeight - margin - POPUP_HEIGHT;
        }
        
        popupX = Math.max(margin, popupX);
        popupY = Math.max(margin, popupY);
        
        PDAnnotationPopup popup = new PDAnnotationPopup();
        popup.setContents(contents);
        popup.setOpen(true);
        popup.setRectangle(new PDRectangle(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT));
        annotation.setPopup(popup);
        
        page.getAnnotations().add(annotation);
        page.getAnnotations().add(popup);
        
        logger.debug("[PDF注释] 添加注释: annotX={}, annotY={}, popupX={}, popupY={}", annotX, annotY, popupX, popupY);
    }

    private static String buildAnnotationContent(DarkDetectResultEntity result) {
        StringBuilder sb = new StringBuilder();
        
        String ruleCode = result.getRuleCode() != null ? result.getRuleCode() : "RULE";
        String ruleName = result.getRuleName();
        if (ruleName == null || ruleName.isEmpty()) {
            ruleName = ruleCode;
        }
        sb.append("[").append(ruleName).append("]\n");
        
        sb.append("页面: ").append(result.getPageNo() != null ? result.getPageNo() : 1).append("\n");
        
        if (result.getParagraphIndex() != null) {
            sb.append("段落: ").append(result.getParagraphIndex() + 1).append("\n");
        }
        
        if (result.getSeverity() != null) {
            String[] levels = {"", "警告", "一般", "严重"};
            if (result.getSeverity() >= 1 && result.getSeverity() <= 3) {
                sb.append("严重程度: ").append(levels[result.getSeverity()]).append("\n");
            }
        }
        
        if ("SENSITIVE_WORD".equals(ruleCode) || "sensitive".equals(result.getRuleCategory())) {
            if (result.getActualValue() != null && !result.getActualValue().isEmpty()) {
                sb.append("敏感词: \"").append(result.getActualValue()).append("\"\n");
            }
            String remark = result.getRemark();
            sb.append("问题: ").append(remark != null && !remark.isEmpty() ? remark : "检测到敏感词").append("\n");
        } else {
            boolean hasDetail = false;
            
            if (result.getActualValue() != null && !result.getActualValue().isEmpty()) {
                sb.append("实际值: ").append(result.getActualValue()).append("\n");
                hasDetail = true;
            }
            
            if (result.getExpectedValue() != null && !result.getExpectedValue().isEmpty()) {
                sb.append("期望值: ").append(result.getExpectedValue()).append("\n");
                hasDetail = true;
            }
            
            String remark = result.getRemark();
            if (remark != null && !remark.isEmpty()) {
                sb.append("说明: ").append(remark).append("\n");
            } else if (hasDetail) {
                sb.append("说明: 格式不符合要求\n");
            } else {
                sb.append("说明: 检测到问题，请检查\n");
            }
        }
        
        return sb.toString();
    }

    private static class TextPositionInfo {
        float x1, y1, x2, y2;
        String text;
        
        TextPositionInfo(float x1, float y1, float x2, float y2, String text) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.text = text;
        }
    }

    private static class ImagePositionInfo {
        float x1, y1, x2, y2;
        
        ImagePositionInfo(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    private static class ImagePositionExtractor extends PDFStreamEngine {
        private List<ImagePositionInfo> imagePositions = new ArrayList<>();
        private float currentX = 0;
        private float currentY = 0;
        private float currentScaleX = 1;
        private float currentScaleY = 1;
        
        public List<ImagePositionInfo> getImagePositions() {
            return imagePositions;
        }
        
        @Override
        protected void processOperator(Operator operator, List<org.apache.pdfbox.cos.COSBase> operands) throws IOException {
            String operatorName = operator.getName();
            
            if (OperatorName.CONCAT.equals(operatorName) && operands.size() >= 6) {
                try {
                    float a = ((org.apache.pdfbox.cos.COSNumber) operands.get(0)).floatValue();
                    float b = ((org.apache.pdfbox.cos.COSNumber) operands.get(1)).floatValue();
                    float c = ((org.apache.pdfbox.cos.COSNumber) operands.get(2)).floatValue();
                    float d = ((org.apache.pdfbox.cos.COSNumber) operands.get(3)).floatValue();
                    float e = ((org.apache.pdfbox.cos.COSNumber) operands.get(4)).floatValue();
                    float f = ((org.apache.pdfbox.cos.COSNumber) operands.get(5)).floatValue();
                    
                    float newX = currentX * a + currentY * c + e;
                    float newY = currentX * b + currentY * d + f;
                    float newScaleX = currentScaleX * a;
                    float newScaleY = currentScaleY * d;
                    
                    currentX = newX;
                    currentY = newY;
                    currentScaleX = newScaleX;
                    currentScaleY = newScaleY;
                } catch (Exception e) {
                    // ignore
                }
            } else if (OperatorName.DRAW_OBJECT.equals(operatorName)) {
                if (!operands.isEmpty()) {
                    org.apache.pdfbox.cos.COSBase base = operands.get(0);
                    if (base instanceof org.apache.pdfbox.cos.COSName) {
                        org.apache.pdfbox.cos.COSName name = (org.apache.pdfbox.cos.COSName) base;
                        PDResources resources = getResources();
                        if (resources != null) {
                            PDXObject xobject = resources.getXObject(name);
                            if (xobject instanceof PDImageXObject) {
                                PDImageXObject image = (PDImageXObject) xobject;
                                
                                float imgWidth = image.getWidth() * Math.abs(currentScaleX);
                                float imgHeight = image.getHeight() * Math.abs(currentScaleY);
                                
                                float x = currentX;
                                float y = currentY;
                                
                                if (currentScaleY < 0) {
                                    y = y - imgHeight;
                                }
                                
                                imagePositions.add(new ImagePositionInfo(x, y, x + imgWidth, y + imgHeight));
                                
                                logger.debug("[PDF图片提取] 解析位置: x={}, y={}, width={}, height={}", x, y, imgWidth, imgHeight);
                            }
                        }
                    }
                }
            }
            
            super.processOperator(operator, operands);
        }
    }
}