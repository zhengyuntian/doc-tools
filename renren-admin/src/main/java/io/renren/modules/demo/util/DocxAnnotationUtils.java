package io.renren.modules.demo.util;

import io.renren.modules.demo.entity.DarkDetectResultEntity;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DocxAnnotationUtils {

    public static File addAnnotations(String filePath, List<DarkDetectResultEntity> results) throws Exception {
        File originalFile = new File(filePath);
        if (!originalFile.exists()) {
            return originalFile;
        }

        File tempFile = File.createTempFile("annotated_", ".docx");

        try (FileInputStream fis = new FileInputStream(originalFile);
             XWPFDocument document = new XWPFDocument(fis);
             FileOutputStream fos = new FileOutputStream(tempFile)) {

            if (results == null || results.isEmpty()) {
                document.write(fos);
                return tempFile;
            }

            XWPFComments comments = document.createComments();
            BigInteger commentId = BigInteger.valueOf(1);
            
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            // 统计图片数量
            int totalImages = countImages(document);
            int imageIndex = 0;

            for (DarkDetectResultEntity result : results) {
                if (result.getIsPass() != null && result.getIsPass() == 0) {
                    boolean isImageResult = "image".equals(result.getRuleCategory()) 
                        || result.getRemark() != null && result.getRemark().contains("图片")
                        || result.getActualValue() != null && result.getActualValue().contains("图片");
                    
                    if (isImageResult) {
                        // 在图片所在位置添加批注
                        imageIndex = addImageAnnotation(document, comments, result, commentId, imageIndex);
                        commentId = commentId.add(BigInteger.ONE);
                    } else {
                        // 文本标注：按段落索引定位，并添加高亮
                        int paraIndex = result.getParagraphIndex() != null ? result.getParagraphIndex() : 0;
                        
                        if (paraIndex >= 0 && paraIndex < paragraphs.size()) {
                            XWPFParagraph paragraph = paragraphs.get(paraIndex);
                            addTextAnnotation(document, comments, paragraph, result, commentId);
                            commentId = commentId.add(BigInteger.ONE);
                        } else if (!paragraphs.isEmpty()) {
                            addTextAnnotation(document, comments, paragraphs.get(0), result, commentId);
                            commentId = commentId.add(BigInteger.ONE);
                        }
                    }
                }
            }

            document.write(fos);
        }

        return tempFile;
    }
    
    private static int countImages(XWPFDocument document) {
        int count = 0;
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                for (XWPFPicture picture : run.getEmbeddedPictures()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int addImageAnnotation(XWPFDocument document, XWPFComments comments,
                                          DarkDetectResultEntity result, BigInteger commentId, int startImageIndex) {
        String author = "暗标检测系统";
        
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("【").append(result.getRuleName()).append("】\n");
        textBuilder.append("违规描述：").append(result.getRemark() != null ? result.getRemark() : "图片存在问题").append("\n");
        if (result.getActualValue() != null) {
            textBuilder.append("图片信息：").append(result.getActualValue()).append("\n");
        }
        if (result.getPageNo() != null) {
            textBuilder.append("页码：").append(result.getPageNo());
        }

        XWPFComment comment = comments.createComment(commentId);
        comment.setAuthor(author);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        comment.setDate(calendar);
        
        XWPFParagraph commentPara = comment.createParagraph();
        XWPFRun run = commentPara.createRun();
        run.setText(textBuilder.toString());
        
        // 遍历段落查找图片
        int currentImageIndex = 0;
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun runWithImage : paragraph.getRuns()) {
                for (XWPFPicture picture : runWithImage.getEmbeddedPictures()) {
                    if (currentImageIndex >= startImageIndex) {
                        // 在图片所在的 run 后面添加批注
                        CTP ctp = paragraph.getCTP();
                        List<CTR> ctrList = ctp.getRList();
                        
                        int runIndex = -1;
                        for (int i = 0; i < ctrList.size(); i++) {
                            if (ctrList.get(i).equals(runWithImage.getCTR())) {
                                runIndex = i;
                                break;
                            }
                        }
                        
                        if (runIndex >= 0) {
                            CTMarkupRange start = ctp.addNewCommentRangeStart();
                            start.setId(commentId);
                            
                            CTR ctr = ctp.addNewR();
                            CTText ctText = ctr.addNewT();
                            ctText.setStringValue("");
                            
                            CTMarkupRange end = ctp.addNewCommentRangeEnd();
                            end.setId(commentId);
                            
                            CTMarkup commentRef = ctr.addNewCommentReference();
                            commentRef.setId(commentId);
                            
                            return currentImageIndex + 1;
                        }
                    }
                    currentImageIndex++;
                }
            }
        }
        
        return currentImageIndex;
    }

    private static void addTextAnnotation(XWPFDocument document, XWPFComments comments,
                                          XWPFParagraph paragraph, DarkDetectResultEntity result, BigInteger commentId) {
        String author = "暗标检测系统";
        
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("【").append(result.getRuleName()).append("】\n");
        textBuilder.append("违规描述：").append(result.getRemark() != null ? result.getRemark() : "不符合规则要求").append("\n");
        if (result.getActualValue() != null) {
            textBuilder.append("实际值：").append(result.getActualValue()).append("\n");
        }
        if (result.getExpectedValue() != null) {
            textBuilder.append("期望值：").append(result.getExpectedValue()).append("\n");
        }
        if (result.getPageNo() != null) {
            textBuilder.append("页码：").append(result.getPageNo());
        }

        XWPFComment comment = comments.createComment(commentId);
        comment.setAuthor(author);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        comment.setDate(calendar);
        
        XWPFParagraph commentPara = comment.createParagraph();
        XWPFRun run = commentPara.createRun();
        run.setText(textBuilder.toString());

        CTP ctp = paragraph.getCTP();
        
        CTMarkupRange start = ctp.addNewCommentRangeStart();
        start.setId(commentId);
        
        CTR ctr = ctp.addNewR();
        CTText ctText = ctr.addNewT();
        ctText.setStringValue(" ");
        
        CTMarkupRange end = ctp.addNewCommentRangeEnd();
        end.setId(commentId);
        
        CTMarkup commentRef = ctr.addNewCommentReference();
        commentRef.setId(commentId);
        
        // 添加高亮：为段落中的所有 run 添加黄色底纹
        for (XWPFRun r : paragraph.getRuns()) {
            r.getCTR().addNewRPr().addNewShd().setFill("FFFF00");
        }
    }
}
