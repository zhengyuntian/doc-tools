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
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            BigInteger commentId = BigInteger.valueOf(1);

            for (DarkDetectResultEntity result : results) {
                if (result.getIsPass() != null && result.getIsPass() == 0) {
                    int paraIndex = result.getParagraphIndex() != null ? result.getParagraphIndex() : 0;
                    
                    if (paraIndex >= 0 && paraIndex < paragraphs.size()) {
                        XWPFParagraph paragraph = paragraphs.get(paraIndex);
                        addComment(document, comments, paragraph, result, commentId);
                        commentId = commentId.add(BigInteger.ONE);
                    } else if (!paragraphs.isEmpty()) {
                        addComment(document, comments, paragraphs.get(0), result, commentId);
                        commentId = commentId.add(BigInteger.ONE);
                    }
                }
            }

            document.write(fos);
        }

        return tempFile;
    }

    private static void addComment(XWPFDocument document, XWPFComments comments,
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
    }
}