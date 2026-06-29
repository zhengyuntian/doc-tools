package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FontRuleExecutor implements RuleExecutor {

    @Override
    public List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity rule) {
        List<DarkDetectResultEntity> results = new ArrayList<>();
        String ruleCode = rule.getRuleCode();
        String ruleName = rule.getRuleName();
        String expectedValue = rule.getParamValue();

        if (ruleCode == null || expectedValue == null) {
            return results;
        }

        switch (ruleCode) {
            case "TEXT_COLOR":
                checkTextColor(document, rule, results);
                break;
            case "ENGLISH_PUNCTUATION":
                checkEnglishPunctuation(document, rule, results);
                break;
            case "FONT_SCALING":
                checkFontScaling(document, rule, results);
                break;
            case "BODY_FONT":
                checkBodyFont(document, rule, results);
                break;
            case "BODY_FONT_SIZE":
                checkBodyFontSize(document, rule, results);
                break;
            case "CHARACTER_SPACING":
                checkCharacterSpacing(document, rule, results);
                break;
            default:
                break;
        }

        if (results.isEmpty()) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setRuleCode(ruleCode);
            passResult.setRuleName(ruleName);
            passResult.setRuleCategory("font");
            passResult.setIsPass(1);
            passResult.setRemark(ruleName + "通过检查");
            results.add(passResult);
        }

        return results;
    }

    private void checkTextColor(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            String actual = document.getParagraphs().get(i).getColor();
            if (actual == null) actual = "black";
            if (!actual.equalsIgnoreCase(expected)) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                        document.getParagraphs().get(i).getPageNo(), i,
                        actual, expected, "段落" + (i + 1) + "文字颜色不符合要求"));
                if (results.size() >= 10) break;
            }
        }
    }

    private void checkEnglishPunctuation(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean useChinesePunctuation = Boolean.parseBoolean(rule.getParamValue());
        if (useChinesePunctuation) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                String text = document.getParagraphs().get(i).getText();
                if (text != null) {
                    if (text.contains(".") || text.contains(",") || text.contains(";") || 
                        text.contains(":") || text.contains("!") || text.contains("?")) {
                        results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                                document.getParagraphs().get(i).getPageNo(), i,
                                "英文标点", "中文标点",
                                "段落" + (i + 1) + "包含英文标点符号"));
                        if (results.size() >= 10) break;
                    }
                }
            }
        }
    }

    private void checkFontScaling(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        boolean allowScaling = Boolean.parseBoolean(rule.getParamValue());
        if (!allowScaling) {
            for (int i = 0; i < document.getParagraphs().size(); i++) {
                Double scale = document.getParagraphs().get(i).getFontScale();
                if (scale != null && scale != 1.0) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                            document.getParagraphs().get(i).getPageNo(), i,
                            String.valueOf(scale), "1.0",
                            "段落" + (i + 1) + "字体缩放不符合要求"));
                    if (results.size() >= 10) break;
                }
            }
        }
    }

    private void checkBodyFont(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            String actual = document.getParagraphs().get(i).getFontFamily();
            if (actual != null && !actual.contains(expected) && !expected.contains(actual)) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                        document.getParagraphs().get(i).getPageNo(), i,
                        actual, expected, "段落" + (i + 1) + "字体不符合要求"));
                if (results.size() >= 10) break;
            }
        }
    }

    private void checkBodyFontSize(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            Double actual = document.getParagraphs().get(i).getFontSize();
            if (actual != null && Math.abs(actual - expected) > 0.5) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                        document.getParagraphs().get(i).getPageNo(), i,
                        String.valueOf(actual), rule.getParamValue(),
                        "段落" + (i + 1) + "字号不符合要求"));
                if (results.size() >= 10) break;
            }
        }
    }

    private void checkCharacterSpacing(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            Double actual = document.getParagraphs().get(i).getCharacterSpacing();
            if (actual != null && Math.abs(actual - expected) > 0.1) {
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                        document.getParagraphs().get(i).getPageNo(), i,
                        String.valueOf(actual), rule.getParamValue(),
                        "段落" + (i + 1) + "字符间距不符合要求"));
                if (results.size() >= 10) break;
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
        return "font".equals(ruleCategory);
    }
}
