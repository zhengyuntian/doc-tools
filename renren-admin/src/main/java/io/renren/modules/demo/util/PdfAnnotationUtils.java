package io.renren.modules.demo.util;

import io.renren.modules.demo.entity.DarkDetectResultEntity;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPopup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PdfAnnotationUtils {

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
                    
                    Map<Integer, TextPositionInfo> textPositionMap = extractTextPositions(document, page);
                    
                    addAnnotationsToPage(page, pageResults, pageIndex + 1, textPositionMap);
                }
            }

            document.save(tempFile);
        }

        return tempFile;
    }

    private static Map<Integer, TextPositionInfo> extractTextPositions(PDDocument document, PDPage page) {
        Map<Integer, TextPositionInfo> map = new HashMap<>();
        
        try {
            PDFTextStripper stripper = new PDFTextStripper() {
                int lineIndex = 0;
                
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (textPositions != null && !textPositions.isEmpty()) {
                        TextPosition first = textPositions.get(0);
                        TextPosition last = textPositions.get(textPositions.size() - 1);
                        
                        map.put(lineIndex, new TextPositionInfo(
                            first.getX(),
                            first.getY(),
                            last.getX() + last.getWidth(),
                            first.getY() - first.getHeight(),
                            text.trim()
                        ));
                        lineIndex++;
                    }
                    super.writeString(text, textPositions);
                }
            };
            
            stripper.setStartPage(document.getPages().indexOf(page) + 1);
            stripper.setEndPage(document.getPages().indexOf(page) + 1);
            stripper.getText(document);
        } catch (IOException e) {
            System.out.println("[PDF标注] 提取文本位置失败: " + e.getMessage());
        }
        
        return map;
    }

    private static void addAnnotationsToPage(PDPage page, List<DarkDetectResultEntity> results, int pageNo, Map<Integer, TextPositionInfo> textPositionMap) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight();
        float margin = 20;
        
        float annotationX = pageWidth - margin - 20;
        float annotationY = pageHeight - margin - 20;
        float annotationGap = 35;
        float minY = margin + 30;
        
        int count = 0;
        for (DarkDetectResultEntity result : results) {
            addHighlightAnnotation(page, result, textPositionMap, mediaBox);
            
            String contents = buildAnnotationContent(result);
            System.out.println("[PDF标注] 批注内容: " + contents.replace("\n", "\\n"));
            
            float iconY = annotationY - (count * annotationGap);
            if (iconY < minY) {
                break;
            }
            
            PDAnnotationText annotation = new PDAnnotationText();
            annotation.setRectangle(new PDRectangle(annotationX, iconY, 18, 18));
            
            PDColor yellow = new PDColor(new float[]{1.0f, 1.0f, 0.8f}, PDDeviceRGB.INSTANCE);
            annotation.setColor(yellow);
            
            annotation.setContents(contents);
            annotation.setLocked(false);
            annotation.setPrinted(true);
            annotation.setName("Comment");
            
            PDAnnotationPopup popup = new PDAnnotationPopup();
            popup.setContents(contents);
            popup.setOpen(true);
            popup.setRectangle(new PDRectangle(annotationX - 280, iconY - 80, 280, 100));
            annotation.setPopup(popup);
            
            page.getAnnotations().add(annotation);
            page.getAnnotations().add(popup);
            
            count++;
        }
    }

    private static void addHighlightAnnotation(PDPage page, DarkDetectResultEntity result, Map<Integer, TextPositionInfo> textPositionMap, PDRectangle mediaBox) throws IOException {
        Integer paragraphIndex = result.getParagraphIndex();
        
        // 只有找到精确位置时才添加高亮，避免误导用户
        if (paragraphIndex != null && textPositionMap.containsKey(paragraphIndex)) {
            String contents = buildAnnotationContent(result);
            
            TextPositionInfo pos = textPositionMap.get(paragraphIndex);
            
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
        
        // 敏感词检测特殊处理
        if ("SENSITIVE_WORD".equals(ruleCode) || "sensitive".equals(result.getRuleCategory())) {
            if (result.getActualValue() != null && !result.getActualValue().isEmpty()) {
                sb.append("敏感词: \"").append(result.getActualValue()).append("\"\n");
            }
            String remark = result.getRemark();
            sb.append("问题: ").append(remark != null && !remark.isEmpty() ? remark : "检测到敏感词").append("\n");
        } else {
            // 方案检测结果
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
}