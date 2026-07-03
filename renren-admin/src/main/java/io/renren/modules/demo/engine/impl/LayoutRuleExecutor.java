package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedPageSetup;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LayoutRuleExecutor implements RuleExecutor {
    
    private int startBodyPage = 1;
    private ParsedDocument document;

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
            case "PAPER_SIZE":
                checkPaperSize(document, rule, results);
                break;
            case "PAPER_ORIENTATION":
                checkPaperOrientation(document, rule, results);
                break;
            case "PAGE_MARGIN":
                checkPageMargin(document, rule, results, paramKey);
                break;
            case "HEADER_FOOTER":
                checkHeaderFooter(document, rule, results, paramKey);
                break;
            case "CONTENT_CHECK":
                checkContent(document, rule, results, paramKey);
                break;
            case "COVER_CHECK":
                checkCover(document, rule, results);
                break;
            case "PAGE_NUMBER_CHECK":
                checkPageNumber(document, rule, results);
                break;
            case "PARAGRAPH_ALIGN":
                checkParagraphAlign(document, rule, results);
                break;
            case "LINE_SPACING":
                checkLineSpacing(document, rule, results);
                break;
            case "FIRST_LINE_INDENT":
                checkFirstLineIndent(document, rule, results);
                break;
            case "PARAGRAPH_SPACE":
                checkParagraphSpace(document, rule, results, paramKey);
                break;
            case "SPECIAL_FORMAT":
                checkSpecialFormat(document, rule, results);
                break;
            case "BACKGROUND_SHADING":
                checkBackgroundShading(document, rule, results);
                break;
            case "PARAGRAPH_PROPERTY":
                checkParagraphProperty(document, rule, results, paramKey);
                break;
            case "SPACE_CHECK":
                checkSpace(document, rule, results);
                break;
            case "FIGURE_ALIGN":
                checkFigureAlign(document, rule, results);
                break;
            default:
                break;
        }

        if (results.isEmpty()) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setRuleCode(ruleCode);
            passResult.setRuleName(ruleName);
            passResult.setRuleCategory("layout");
            passResult.setIsPass(1);
            passResult.setRemark(ruleName + "通过检查");
            results.add(passResult);
        }

        return results;
    }

    private void checkPaperSize(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        ParsedPageSetup pageSetup = document.getPageSetup();
        if (pageSetup != null && pageSetup.getPaperSize() != null) {
            String actualSize = pageSetup.getPaperSize();
            if (!actualSize.equalsIgnoreCase(rule.getParamValue())) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout", 1, 1,
                        actualSize, rule.getParamValue(),
                        "纸张大小不符合要求，当前为" + actualSize));
            }
        }
    }

    private void checkPaperOrientation(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        ParsedPageSetup pageSetup = document.getPageSetup();
        if (pageSetup != null) {
            String expected = rule.getParamValue();
            String actual = "portrait";
            if (!actual.equalsIgnoreCase(expected)) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout", 1, 1,
                        actual, expected, "纸张方向不符合要求"));
            }
        }
    }

    private void checkPageMargin(ParsedDocument document, DarkRuleConfigEntity rule, 
                                  List<DarkDetectResultEntity> results, String paramKey) {
        ParsedPageSetup pageSetup = document.getPageSetup();
        if (pageSetup == null) return;

        double expectedValue = Double.parseDouble(rule.getParamValue());
        double actualValue = 0;
        String marginName = "";

        switch (paramKey) {
            case "margin_top":
                actualValue = pageSetup.getMarginTop();
                marginName = "上边距";
                break;
            case "margin_bottom":
                actualValue = pageSetup.getMarginBottom();
                marginName = "下边距";
                break;
            case "margin_left":
                actualValue = pageSetup.getMarginLeft();
                marginName = "左边距";
                break;
            case "margin_right":
                actualValue = pageSetup.getMarginRight();
                marginName = "右边距";
                break;
        }

        if (Math.abs(actualValue - expectedValue) > 0.1) {
            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout", 1, 1,
                    String.valueOf(actualValue), rule.getParamValue(),
                    marginName + "不符合要求，当前为" + actualValue + "cm"));
        }
    }

    private void checkParagraphAlign(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            String actual = para.getAlignment();
            String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
            if (actual == null) actual = "left";
            if (!actual.equalsIgnoreCase(expected)) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout", 
                        para.getPageNo(), i,
                        alignmentToChinese(actual), alignmentToChinese(expected), 
                        "段落" + (i + 1) + "对齐方式不符合要求，内容：" + previewText));
            }
        }
    }
    
    private String alignmentToChinese(String alignment) {
        if (alignment == null) return "左对齐";
        return switch (alignment.toLowerCase()) {
            case "left" -> "左对齐";
            case "center" -> "居中";
            case "right" -> "右对齐";
            default -> alignment;
        };
    }

    private void checkLineSpacing(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            Double actual = para.getLineSpacingValue();
            String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
            if (actual != null && actual > 0 && Math.abs(actual - expected) > 1) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        para.getPageNo(), i,
                        String.valueOf(actual), rule.getParamValue(),
                        "段落" + (i + 1) + "行间距不符合要求，内容：" + previewText));
            }
        }
    }

    private void checkFirstLineIndent(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        int expectedChars = Integer.parseInt(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            String text = para.getText();
            if (text != null && !text.isEmpty()) {
                int leadingSpaces = 0;
                while (leadingSpaces < text.length() && text.charAt(leadingSpaces) == ' ') {
                    leadingSpaces++;
                }
                if (leadingSpaces != expectedChars * 2) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i,
                            String.valueOf(leadingSpaces / 2), rule.getParamValue(),
                            "段落" + (i + 1) + "首行缩进不符合要求，内容：" + (text.length() > 20 ? text.substring(0, 20) + "..." : text)));
                }
            }
        }
    }

    private void checkParagraphSpace(ParsedDocument document, DarkRuleConfigEntity rule, 
                                      List<DarkDetectResultEntity> results, String paramKey) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            Double actualDouble = "space_before".equals(paramKey) ? 
                    para.getSpaceBefore() : 
                    para.getSpaceAfter();
            if (actualDouble == null) continue;
            double actual = actualDouble.doubleValue();
            if (Math.abs(actual - expected) > 1) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        para.getPageNo(), i,
                        String.valueOf(actual), rule.getParamValue(),
                        "段落" + (i + 1) + ("space_before".equals(paramKey) ? "段前" : "段后") + "间距不符合要求"));
            }
        }
    }

    private void checkSpecialFormat(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String[] forbiddenStyles = rule.getParamValue().split(",");
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            String paraText = para.getText();
            String previewText = paraText.length() > 20 ? paraText.substring(0, 20) + "..." : paraText;
            for (String style : forbiddenStyles) {
                if ("bold".equals(style) && Boolean.TRUE.equals(para.getBold())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i, "加粗", "不加粗",
                            "段落" + (i + 1) + "包含禁止的加粗格式，内容：" + previewText));
                } else if ("underline".equals(style) && Boolean.TRUE.equals(para.getUnderline())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i, "下划线", "无下划线",
                            "段落" + (i + 1) + "包含禁止的下划线格式，内容：" + previewText));
                } else if ("italic".equals(style) && Boolean.TRUE.equals(para.getItalic())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i, "斜体", "无斜体",
                            "段落" + (i + 1) + "包含禁止的斜体格式，内容：" + previewText));
                }
            }
        }
    }

    private void checkBackgroundShading(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean noShading = Boolean.parseBoolean(rule.getParamValue());
        if (noShading) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                var para = document.getParagraphs().get(i);
                if (para.getPageNo() < startBodyPage) continue;
                
                if (Boolean.TRUE.equals(para.getShading())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i,
                            "有底纹", "无底纹",
                            "段落" + (i + 1) + "包含底纹"));
                }
            }
        }
    }

    private void checkParagraphProperty(ParsedDocument document, DarkRuleConfigEntity rule, 
                                         List<DarkDetectResultEntity> results, String paramKey) {
        if ("direction".equals(paramKey)) {
            String expected = rule.getParamValue();
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                var para = document.getParagraphs().get(i);
                if (para.getPageNo() < startBodyPage) continue;
                
                String actual = para.getDirection();
                if (actual == null) actual = "left_to_right";
                if (!actual.equalsIgnoreCase(expected)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i,
                            actual, expected, "段落" + (i + 1) + "输入方向不符合要求"));
                }
            }
        }
    }

    private void checkSpace(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean checkSpaces = Boolean.parseBoolean(rule.getParamValue());
        if (checkSpaces) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                var para = document.getParagraphs().get(i);
                if (para.getPageNo() < startBodyPage) continue;
                
                String text = para.getText();
                if (text != null && text.contains("  ")) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i,
                            "多个空格", "单个空格",
                            "段落" + (i + 1) + "包含连续空格"));
                }
            }
        }
    }

    private void checkHeaderFooter(ParsedDocument document, DarkRuleConfigEntity rule, 
                                    List<DarkDetectResultEntity> results, String paramKey) {
        boolean checkEnabled = Boolean.parseBoolean(rule.getParamValue());
        if (!checkEnabled) return;

        ParsedPageSetup pageSetup = document.getPageSetup();
        if (pageSetup == null) return;

        if ("check_header".equals(paramKey)) {
            if (Boolean.TRUE.equals(pageSetup.getHasHeader())) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        1, 0, "has_header", "no_header", "文档包含页眉"));
            }
        } else if ("check_footer".equals(paramKey)) {
            if (Boolean.TRUE.equals(pageSetup.getHasFooter())) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        1, 0, "has_footer", "no_footer", "文档包含页脚"));
            }
        }
    }

    private void checkContent(ParsedDocument document, DarkRuleConfigEntity rule, 
                              List<DarkDetectResultEntity> results, String paramKey) {
        boolean checkEnabled = Boolean.parseBoolean(rule.getParamValue());
        if (!checkEnabled) return;

        String fullText = document.getFullText();
        if (fullText == null) return;

        if ("check_table_of_contents".equals(paramKey)) {
            if (fullText.contains("目录") || fullText.contains("Contents") || fullText.contains("TABLE OF CONTENTS")) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        1, 0, "包含目录", "无目录", "文档包含目录"));
            }
        }
    }

    private void checkCover(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean checkEnabled = Boolean.parseBoolean(rule.getParamValue());
        if (!checkEnabled) return;

        String fullText = document.getFullText();
        if (fullText == null) return;

        if (fullText.contains("封面") || fullText.contains("COVER")) {
            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                    1, 0, "has_cover", "no_cover", "文档包含封面"));
        }
    }

    private void checkPageNumber(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean checkEnabled = Boolean.parseBoolean(rule.getParamValue());
        if (!checkEnabled) return;

        String fullText = document.getFullText();
        if (fullText == null) return;

        if (fullText.contains("页码") || fullText.contains("Page") || fullText.contains("PAGE")) {
            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                    1, 0, "has_page_number", "no_page_number", "文档包含页码"));
        }
    }

    private void checkFigureAlign(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        java.util.Set<String> processedKeys = new java.util.HashSet<>();

        if (document.getTables() != null) {
            for (int i = 0; i < document.getTables().size(); i++) {
                var table = document.getTables().get(i);
                if (table.getPageNo() > 0 && table.getPageNo() < startBodyPage) continue;
                
                String resultKey = rule.getRuleCode() + "_table_" + table.getPageNo() + "_" + i;
                if (processedKeys.contains(resultKey)) continue;
                
                String actual = table.getAlignment();
                if (actual == null) actual = "left";
                if (!actual.equalsIgnoreCase(expected)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            table.getPageNo() > 0 ? table.getPageNo() : 1, i, 
                            alignmentToChinese(actual), alignmentToChinese(expected), "表格" + (i + 1) + "整体对齐方式不符合要求"));
                    processedKeys.add(resultKey);
                }
            }
        }

        if (document.getImages() != null) {
            for (int i = 0; i < document.getImages().size(); i++) {
                var image = document.getImages().get(i);
                if (image.getPageNo() > 0 && image.getPageNo() < startBodyPage) continue;
                
                String resultKey = rule.getRuleCode() + "_image_" + image.getPageNo() + "_" + i;
                if (processedKeys.contains(resultKey)) continue;
                
                String actual = image.getAlignment();
                if (actual == null) actual = "left";
                if (!actual.equalsIgnoreCase(expected)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            image.getPageNo() > 0 ? image.getPageNo() : 1, i,
                            alignmentToChinese(actual), alignmentToChinese(expected), "图片" + (i + 1) + "整体对齐方式不符合要求"));
                    processedKeys.add(resultKey);
                }
            }
        }
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
        return "layout".equals(ruleCategory);
    }
}
