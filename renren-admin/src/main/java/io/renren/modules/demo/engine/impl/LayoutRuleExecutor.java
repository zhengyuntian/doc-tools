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

    @Override
    public List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity rule) {
        List<DarkDetectResultEntity> results = new ArrayList<>();
        String ruleCode = rule.getRuleCode();
        String ruleName = rule.getRuleName();
        String paramKey = rule.getParamKey();
        String expectedValue = rule.getParamValue();

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
            String actual = document.getParagraphs().get(i).getAlignment();
            if (actual == null) actual = "left";
            if (!actual.equalsIgnoreCase(expected)) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout", 
                        document.getParagraphs().get(i).getPageNo(), i,
                        actual, expected, "段落" + (i + 1) + "对齐方式不符合要求"));
                if (results.size() >= 10) break;
            }
        }
    }

    private void checkLineSpacing(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            Double actual = document.getParagraphs().get(i).getLineSpacingValue();
            if (actual != null && actual > 0 && Math.abs(actual - expected) > 1) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        document.getParagraphs().get(i).getPageNo(), i,
                        String.valueOf(actual), rule.getParamValue(),
                        "段落" + (i + 1) + "行间距不符合要求"));
                if (results.size() >= 10) break;
            }
        }
    }

    private void checkFirstLineIndent(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        int expectedChars = Integer.parseInt(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            String text = document.getParagraphs().get(i).getText();
            if (text != null && !text.isEmpty()) {
                int leadingSpaces = 0;
                while (leadingSpaces < text.length() && text.charAt(leadingSpaces) == ' ') {
                    leadingSpaces++;
                }
                if (leadingSpaces != expectedChars * 2) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            document.getParagraphs().get(i).getPageNo(), i,
                            String.valueOf(leadingSpaces / 2), rule.getParamValue(),
                            "段落" + (i + 1) + "首行缩进不符合要求"));
                    if (results.size() >= 10) break;
                }
            }
        }
    }

    private void checkParagraphSpace(ParsedDocument document, DarkRuleConfigEntity rule, 
                                      List<DarkDetectResultEntity> results, String paramKey) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            Double actualDouble = "space_before".equals(paramKey) ? 
                    document.getParagraphs().get(i).getSpaceBefore() : 
                    document.getParagraphs().get(i).getSpaceAfter();
            if (actualDouble == null) continue;
            double actual = actualDouble.doubleValue();
            if (Math.abs(actual - expected) > 1) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                        document.getParagraphs().get(i).getPageNo(), i,
                        String.valueOf(actual), rule.getParamValue(),
                        "段落" + (i + 1) + ("space_before".equals(paramKey) ? "段前" : "段后") + "间距不符合要求"));
                if (results.size() >= 10) break;
            }
        }
    }

    private void checkSpecialFormat(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String[] forbiddenStyles = rule.getParamValue().split(",");
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            for (String style : forbiddenStyles) {
                if ("bold".equals(style) && Boolean.TRUE.equals(para.getBold())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i, "bold", "not_bold",
                            "段落" + (i + 1) + "包含禁止的加粗格式"));
                } else if ("underline".equals(style) && Boolean.TRUE.equals(para.getUnderline())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i, "underline", "no_underline",
                            "段落" + (i + 1) + "包含禁止的下划线格式"));
                } else if ("italic".equals(style) && Boolean.TRUE.equals(para.getItalic())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            para.getPageNo(), i, "italic", "no_italic",
                            "段落" + (i + 1) + "包含禁止的斜体格式"));
                }
            }
            if (results.size() >= 10) break;
        }
    }

    private void checkBackgroundShading(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean noShading = Boolean.parseBoolean(rule.getParamValue());
        if (noShading) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                if (Boolean.TRUE.equals(document.getParagraphs().get(i).getShading())) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            document.getParagraphs().get(i).getPageNo(), i,
                            "shading", "no_shading",
                            "段落" + (i + 1) + "包含底纹"));
                    if (results.size() >= 10) break;
                }
            }
        }
    }

    private void checkParagraphProperty(ParsedDocument document, DarkRuleConfigEntity rule, 
                                         List<DarkDetectResultEntity> results, String paramKey) {
        if ("direction".equals(paramKey)) {
            String expected = rule.getParamValue();
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                String actual = document.getParagraphs().get(i).getDirection();
                if (actual == null) actual = "left_to_right";
                if (!actual.equalsIgnoreCase(expected)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            document.getParagraphs().get(i).getPageNo(), i,
                            actual, expected, "段落" + (i + 1) + "输入方向不符合要求"));
                    if (results.size() >= 10) break;
                }
            }
        }
    }

    private void checkSpace(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean checkSpaces = Boolean.parseBoolean(rule.getParamValue());
        if (checkSpaces) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                String text = document.getParagraphs().get(i).getText();
                if (text != null && text.contains("  ")) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "layout",
                            document.getParagraphs().get(i).getPageNo(), i,
                            "multiple_spaces", "single_space",
                            "段落" + (i + 1) + "包含连续空格"));
                    if (results.size() >= 10) break;
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
        result.setPageNo(pageNo);
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
