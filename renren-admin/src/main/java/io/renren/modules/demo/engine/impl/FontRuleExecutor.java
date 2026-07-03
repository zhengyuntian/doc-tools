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
    
    private int startBodyPage = 1;
    private ParsedDocument document;

    @Override
    public List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity rule) {
        List<DarkDetectResultEntity> results = new ArrayList<>();
        String ruleCode = rule.getRuleCode();
        String ruleName = rule.getRuleName();
        String expectedValue = rule.getParamValue();
        
        this.document = document;
        this.startBodyPage = document.getStartBodyPage();

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
                }
            }
        }
    }

    private void checkBodyFont(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            String actual = para.getFontFamily();
            String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
            if (actual != null && !actual.contains(expected) && !expected.contains(actual)) {
                String actualChinese = fontNameToChinese(actual);
                String expectedChinese = fontNameToChinese(expected);
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                        para.getPageNo(), i,
                        actualChinese, expectedChinese, 
                        "段落" + (i + 1) + "字体不符合要求，内容：" + previewText));
            }
        }
    }
    
    private String fontNameToChinese(String fontName) {
        if (fontName == null) return "未知字体";
        fontName = fontName.toLowerCase();
        if (fontName.contains("simsun") || fontName.contains("宋体")) return "宋体";
        if (fontName.contains("song") || fontName.contains("simfang")) return "仿宋";
        if (fontName.contains("kaiti") || fontName.contains("simkai")) return "楷体";
        if (fontName.contains("hei") || fontName.contains("simhei")) return "黑体";
        if (fontName.contains("arial")) return "Arial";
        if (fontName.contains("times")) return "Times New Roman";
        if (fontName.contains("calibri")) return "Calibri";
        return fontName;
    }

    private void checkBodyFontSize(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        String expectedSizeStr = fontSizeToChinese(expected);
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            
            Double actual = para.getFontSize();
            String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
            if (actual != null && Math.abs(actual - expected) > 0.5) {
                String actualSizeStr = fontSizeToChinese(actual);
                results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "font",
                        para.getPageNo(), i,
                        actualSizeStr, expectedSizeStr,
                        "段落" + (i + 1) + "字号不符合要求，内容：" + previewText));
            }
        }
    }
    
    private String fontSizeToChinese(Double size) {
        if (size == null) return "未知";
        double s = size.doubleValue();
        if (Math.abs(s - 10.5) < 0.1) return "四号(14pt)";
        if (Math.abs(s - 12.0) < 0.1) return "小四(12pt)";
        if (Math.abs(s - 14.0) < 0.1) return "三号(16pt)";
        if (Math.abs(s - 16.0) < 0.1) return "小三(15pt)";
        if (Math.abs(s - 18.0) < 0.1) return "二号(18pt)";
        if (Math.abs(s - 21.0) < 0.1) return "小一(21pt)";
        if (Math.abs(s - 22.0) < 0.1) return "一号(22pt)";
        if (Math.abs(s - 24.0) < 0.1) return "小初(24pt)";
        if (Math.abs(s - 36.0) < 0.1) return "初号(36pt)";
        return String.format("%.1fpt", s);
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
        return "font".equals(ruleCategory);
    }
}
