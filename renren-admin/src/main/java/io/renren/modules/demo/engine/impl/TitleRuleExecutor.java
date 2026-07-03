package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TitleRuleExecutor implements RuleExecutor {
    
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
            case "TITLE_FONT":
                checkTitleFont(document, rule, results);
                break;
            case "TITLE_FONT_SIZE":
                checkTitleFontSize(document, rule, results);
                break;
            case "TITLE_STYLE":
                checkTitleStyle(document, rule, results, paramKey);
                break;
            default:
                break;
        }

        if (results.isEmpty()) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setRuleCode(ruleCode);
            passResult.setRuleName(ruleName);
            passResult.setRuleCategory("title");
            passResult.setIsPass(1);
            passResult.setRemark(ruleName + "通过检查");
            results.add(passResult);
        }

        return results;
    }

    private void checkTitleFont(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        String expected = rule.getParamValue();
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            if (para.getIsTitle() != null && para.getIsTitle()) {
                String actual = para.getFontFamily();
                String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                if (actual != null && !actual.contains(expected) && !expected.contains(actual)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                            para.getPageNo(), i, fontNameToChinese(actual), fontNameToChinese(expected),
                            "段落" + (i + 1) + "标题字体不符合要求，内容：" + previewText));
                }
            }
        }
    }

    private void checkTitleFontSize(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getPageNo() < startBodyPage) continue;
            if (para.getIsTitle() != null && para.getIsTitle()) {
                Double actual = para.getFontSize();
                String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                if (actual != null && Math.abs(actual - expected) > 0.5) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                            para.getPageNo(), i, fontSizeToChinese(actual), fontSizeToChinese(expected),
                            "段落" + (i + 1) + "标题字号不符合要求，内容：" + previewText));
                }
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

    private void checkTitleStyle(ParsedDocument document, DarkRuleConfigEntity rule, 
                                  List<DarkDetectResultEntity> results, String paramKey) {
        switch (paramKey) {
            case "title_bold":
                boolean shouldBeBold = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getPageNo() < startBodyPage) continue;
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                        boolean isBold = Boolean.TRUE.equals(para.getBold());
                        if (shouldBeBold != isBold) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i,
                                    isBold ? "加粗" : "不加粗",
                                    shouldBeBold ? "加粗" : "不加粗",
                                    "段落" + (i + 1) + "标题加粗设置不符合要求，内容：" + previewText));
                        }
                    }
                }
                break;
            case "title_color":
                String expectedColor = rule.getParamValue();
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getPageNo() < startBodyPage) continue;
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                        String actualColor = para.getColor();
                        if (actualColor == null) actualColor = "黑色";
                        if (!actualColor.equalsIgnoreCase(expectedColor)) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, actualColor, expectedColor,
                                    "段落" + (i + 1) + "标题颜色不符合要求，内容：" + previewText));
                        }
                    }
                }
                break;
            case "title_no_italic":
                boolean shouldNotBeItalic = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getPageNo() < startBodyPage) continue;
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                        if (shouldNotBeItalic && Boolean.TRUE.equals(para.getItalic())) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, "斜体", "无斜体",
                                    "段落" + (i + 1) + "标题包含斜体，内容：" + previewText));
                        }
                    }
                }
                break;
            case "title_no_underline":
                boolean shouldNotBeUnderline = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getPageNo() < startBodyPage) continue;
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                        if (shouldNotBeUnderline && Boolean.TRUE.equals(para.getUnderline())) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, "下划线", "无下划线",
                                    "段落" + (i + 1) + "标题包含下划线，内容：" + previewText));
                        }
                    }
                }
                break;
            case "title_no_strikethrough":
                boolean shouldNotBeStrikethrough = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getPageNo() < startBodyPage) continue;
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String previewText = para.getText().length() > 20 ? para.getText().substring(0, 20) + "..." : para.getText();
                        if (shouldNotBeStrikethrough && Boolean.TRUE.equals(para.getStrikethrough())) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, "删除线", "无删除线",
                                    "段落" + (i + 1) + "标题包含删除线，内容：" + previewText));
                        }
                    }
                }
                break;
            case "title_style":
                String expectedStyle = rule.getParamValue();
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String actualStyle = para.getTitleStyle();
                        if (actualStyle == null) actualStyle = "normal";
                        if (!actualStyle.equalsIgnoreCase(expectedStyle)) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, actualStyle, expectedStyle,
                                    "标题" + (i + 1) + "样式不符合要求"));
                        }
                    }
                }
                break;
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
        return "title".equals(ruleCategory);
    }
}
