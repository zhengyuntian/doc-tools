package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedTable;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import io.renren.modules.demo.engine.OCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TableRuleExecutor implements RuleExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(TableRuleExecutor.class);
    
    private int startBodyPage = 1;
    private ParsedDocument document;

    @Autowired(required = false)
    private OCRService ocrService;

    @Override
    public List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity rule) {
        List<DarkDetectResultEntity> results = new ArrayList<>();
        String ruleCode = rule.getRuleCode();
        String ruleName = rule.getRuleName();
        String paramKey = rule.getParamKey();
        String expectedValue = rule.getParamValue();
        
        this.document = document;
        this.startBodyPage = document.getStartBodyPage();

        if (ruleCode == null || expectedValue == null) {
            return results;
        }

        switch (ruleCode) {
            case "TABLE_FONT":
                checkTableFont(document, rule, results);
                break;
            case "TABLE_FONT_SIZE":
                checkTableFontSize(document, rule, results);
                break;
            case "TABLE_TEXT_COLOR":
                checkTableTextColor(document, rule, results);
                break;
            case "TABLE_TEXT_STYLE":
                checkTableTextStyle(document, rule, results, paramKey);
                break;
            case "TABLE_IMAGE_FORBIDDEN":
                checkImageTableForbidden(document, rule, results);
                break;
            case "TABLE_ALIGN":
                checkTableAlign(document, rule, results, paramKey);
                break;
            case "TABLE_INDENT":
                checkTableIndent(document, rule, results);
                break;
            default:
                break;
        }

        if (results.isEmpty()) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setRuleCode(ruleCode);
            passResult.setRuleName(ruleName);
            passResult.setRuleCategory("table");
            passResult.setIsPass(1);
            passResult.setRemark(ruleName + "通过检查");
            results.add(passResult);
        }

        return results;
    }

    private void checkTableFont(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        List<ParsedTable> tables = document.getTables();
        if (tables != null) {
            for (int i = 0; i < tables.size(); i++) {
                ParsedTable table = tables.get(i);
                String actual = table.getFontFamily();
                if (actual != null && !actual.contains(expected) && !expected.contains(actual)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 1, i,
                            actual, expected, "表格" + (i + 1) + "字体不符合要求"));
                }
            }
        }
    }

    private void checkTableFontSize(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        List<ParsedTable> tables = document.getTables();
        if (tables != null) {
            for (int i = 0; i < tables.size(); i++) {
                ParsedTable table = tables.get(i);
                Double actual = table.getFontSize();
                if (actual != null && Math.abs(actual - expected) > 0.5) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 1, i,
                            String.valueOf(actual), rule.getParamValue(),
                            "表格" + (i + 1) + "字号不符合要求"));
                }
            }
        }
    }

    private void checkTableTextColor(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        List<ParsedTable> tables = document.getTables();
        if (tables != null) {
            for (int i = 0; i < tables.size(); i++) {
                ParsedTable table = tables.get(i);
                String actual = table.getColor();
                if (actual == null) actual = "black";
                if (!actual.equalsIgnoreCase(expected)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 1, i,
                            actual, expected, "表格" + (i + 1) + "文字颜色不符合要求"));
                }
            }
        }
    }

    private void checkTableTextStyle(ParsedDocument document, DarkRuleConfigEntity rule, 
                                      List<DarkDetectResultEntity> results, String paramKey) {
        boolean shouldNotHaveStyle = Boolean.parseBoolean(rule.getParamValue());
        List<ParsedTable> tables = document.getTables();
        if (tables != null && shouldNotHaveStyle) {
            for (int i = 0; i < tables.size(); i++) {
                ParsedTable table = tables.get(i);
                String styleName = "";
                boolean hasStyle = false;

                switch (paramKey) {
                    case "no_bold":
                        styleName = "加粗";
                        hasStyle = Boolean.TRUE.equals(table.getBold());
                        break;
                    case "no_italic":
                        styleName = "倾斜";
                        hasStyle = Boolean.TRUE.equals(table.getItalic());
                        break;
                    case "no_underline":
                        styleName = "下划线";
                        hasStyle = Boolean.TRUE.equals(table.getUnderline());
                        break;
                    case "no_strikethrough":
                        styleName = "着重号";
                        hasStyle = Boolean.TRUE.equals(table.getStrikethrough());
                        break;
                }

                if (hasStyle) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 1, i,
                            styleName, "无" + styleName, "表格" + (i + 1) + "包含" + styleName));
                }
            }
        }
    }

    private void checkTableAlign(ParsedDocument document, DarkRuleConfigEntity rule, 
                               List<DarkDetectResultEntity> results, String paramKey) {
        String expected = rule.getParamValue();
        List<ParsedTable> tables = document.getTables();
        if (tables != null) {
            for (int i = 0; i < tables.size(); i++) {
                ParsedTable table = tables.get(i);
                String actual = null;
                String alignName = "";

                if ("vertical_align".equals(paramKey)) {
                    actual = table.getVerticalAlign();
                    alignName = "垂直";
                } else if ("horizontal_align".equals(paramKey)) {
                    actual = table.getHorizontalAlign();
                    alignName = "水平";
                }

                if (actual == null) actual = "center";
                if (!actual.equalsIgnoreCase(expected)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 1, i,
                            actual, expected, "表格" + (i + 1) + alignName + "对齐不符合要求"));
                }
            }
        }
    }

    private void checkTableIndent(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean noIndent = Boolean.parseBoolean(rule.getParamValue());
        if (!noIndent) return;

        List<ParsedTable> tables = document.getTables();
        if (tables != null) {
            for (int i = 0; i < tables.size(); i++) {
                ParsedTable table = tables.get(i);
                List<List<String>> cells = table.getCells();
                for (int rowIdx = 0; rowIdx < cells.size(); rowIdx++) {
                    for (int colIdx = 0; colIdx < cells.get(rowIdx).size(); colIdx++) {
                        String cellText = cells.get(rowIdx).get(colIdx);
                        if (cellText != null && !cellText.isEmpty()) {
                            int leadingSpaces = 0;
                            while (leadingSpaces < cellText.length() && cellText.charAt(leadingSpaces) == ' ') {
                                leadingSpaces++;
                            }
                            if (leadingSpaces > 0) {
                                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 1, i,
                                        String.valueOf(leadingSpaces), "0",
                                        "表格" + (i + 1) + "第" + (rowIdx + 1) + "行第" + (colIdx + 1) + "列存在首行缩进"));
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkImageTableForbidden(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean forbidImageTable = Boolean.parseBoolean(rule.getParamValue());
        
        if (!forbidImageTable) {
            return;
        }

        logger.debug("[图片表格检测] 开始检查文档中的图片表格...");
        logger.debug("[图片表格检测] 文档中的图片数量: {}, 正文开始页: {}", document.getImages().size(), startBodyPage);
        
        if (document.getImages() == null || document.getImages().isEmpty()) {
            return;
        }

        if (ocrService == null) {
            logger.debug("[图片表格检测] OCR服务未注入，跳过检查");
            return;
        }

        java.util.Set<String> processedKeys = new java.util.HashSet<>();

        for (int i = 0; i < document.getImages().size(); i++) {
            logger.debug("[图片表格检测] 检查图片 {}/{}...", (i + 1), document.getImages().size());
            
            io.renren.modules.demo.engine.model.ParsedImage image = document.getImages().get(i);
            
            if (image.getPageNo() < startBodyPage) {
                logger.debug("[图片表格检测] 图片 {} 在封面/目录页，跳过", (i + 1));
                continue;
            }
            
            String imageKey = image.getPageNo() + "_" + i;
            if (processedKeys.contains(imageKey)) {
                logger.debug("[图片表格检测] 图片 {} 已处理过，跳过", (i + 1));
                continue;
            }
            
            try {
                logger.debug("[图片表格检测] 调用OCR表格识别服务...");
                Map<String, Object> ocrResult = ocrService.recognizeTable(image);
                
                logger.debug("[图片表格检测] OCR识别结果: {}", ocrResult);
                
                Boolean success = (Boolean) ocrResult.get("success");
                if (Boolean.TRUE.equals(success)) {
                    logger.debug("[图片表格检测] 图片 {} 被识别为表格，违规！", (i + 1));
                    int logicalPageNo = document.getLogicalPageNo(image.getPageNo());
                    
                    String resultKey = rule.getRuleCode() + "_" + image.getPageNo() + "_" + i;
                    if (!processedKeys.contains(resultKey)) {
                        results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "table", 
                                image.getPageNo(), i,
                                "图片表格", "文本表格", "页面" + logicalPageNo + "包含图片格式的表格（图片" + (i + 1) + "）"));
                        processedKeys.add(resultKey);
                    }
                } else {
                    logger.debug("[图片表格检测] 图片 {} 不是表格，继续检查下一张", (i + 1));
                }
            } catch (Exception e) {
                logger.error("[图片表格检测] 图片 {} OCR识别失败: {}", (i + 1), e.getMessage());
            }
        }
        
        logger.debug("[图片表格检测] 检查完成，发现违规图片表格数量: {}", results.size());
    }

    private DarkDetectResultEntity createResult(String ruleCode, String ruleName, String category, 
                                                 int pageNo, int index, String actualValue, 
                                                 String expectedValue, String remark) {
        DarkDetectResultEntity result = new DarkDetectResultEntity();
        result.setRuleCode(ruleCode);
        result.setRuleName(ruleName);
        result.setRuleCategory(category);
        result.setIsPass(0);
        result.setSeverity(1);
        int logicalPageNo = document.getLogicalPageNo(pageNo);
        result.setPageNo(logicalPageNo);
        result.setParagraphIndex(index);
        result.setActualValue(actualValue);
        result.setExpectedValue(expectedValue);
        result.setRemark(remark);
        return result;
    }

    @Override
    public boolean supports(String ruleCategory) {
        return "table".equals(ruleCategory);
    }
}
