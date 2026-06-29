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
            if (para.getIsTitle() != null && para.getIsTitle()) {
                String actual = para.getFontFamily();
                if (actual != null && !actual.contains(expected) && !expected.contains(actual)) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                            para.getPageNo(), i, actual, expected,
                            "标题" + (i + 1) + "字体不符合要求"));
                    if (results.size() >= 10) break;
                }
            }
        }
    }

    private void checkTitleFontSize(ParsedDocument document, DarkRuleConfigEntity rule, List<DarkDetectResultEntity> results) {
        double expected = Double.parseDouble(rule.getParamValue());
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            var para = document.getParagraphs().get(i);
            if (para.getIsTitle() != null && para.getIsTitle()) {
                Double actual = para.getFontSize();
                if (actual != null && Math.abs(actual - expected) > 0.5) {
                    results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                            para.getPageNo(), i, String.valueOf(actual), rule.getParamValue(),
                            "标题" + (i + 1) + "字号不符合要求"));
                    if (results.size() >= 10) break;
                }
            }
        }
    }

    private void checkTitleStyle(ParsedDocument document, DarkRuleConfigEntity rule, 
                                  List<DarkDetectResultEntity> results, String paramKey) {
        switch (paramKey) {
            case "title_bold":
                boolean shouldBeBold = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        boolean isBold = Boolean.TRUE.equals(para.getBold());
                        if (shouldBeBold != isBold) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i,
                                    isBold ? "bold" : "not_bold",
                                    shouldBeBold ? "bold" : "not_bold",
                                    "标题" + (i + 1) + "加粗设置不符合要求"));
                            if (results.size() >= 10) break;
                        }
                    }
                }
                break;
            case "title_color":
                String expectedColor = rule.getParamValue();
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        String actualColor = para.getColor();
                        if (actualColor == null) actualColor = "black";
                        if (!actualColor.equalsIgnoreCase(expectedColor)) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, actualColor, expectedColor,
                                    "标题" + (i + 1) + "颜色不符合要求"));
                            if (results.size() >= 10) break;
                        }
                    }
                }
                break;
            case "title_no_italic":
                boolean shouldNotBeItalic = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        if (shouldNotBeItalic && Boolean.TRUE.equals(para.getItalic())) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, "italic", "no_italic",
                                    "标题" + (i + 1) + "包含斜体"));
                            if (results.size() >= 10) break;
                        }
                    }
                }
                break;
            case "title_no_underline":
                boolean shouldNotBeUnderline = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        if (shouldNotBeUnderline && Boolean.TRUE.equals(para.getUnderline())) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, "underline", "no_underline",
                                    "标题" + (i + 1) + "包含下划线"));
                            if (results.size() >= 10) break;
                        }
                    }
                }
                break;
            case "title_no_strikethrough":
                boolean shouldNotBeStrikethrough = Boolean.parseBoolean(rule.getParamValue());
                for (int i = 0; i < document.getParagraphs().size(); i++) {
                    var para = document.getParagraphs().get(i);
                    if (para.getIsTitle() != null && para.getIsTitle()) {
                        if (shouldNotBeStrikethrough && Boolean.TRUE.equals(para.getStrikethrough())) {
                            results.add(createResult(rule.getRuleCode(), rule.getRuleName(), "title",
                                    para.getPageNo(), i, "strikethrough", "no_strikethrough",
                                    "标题" + (i + 1) + "包含着重号"));
                            if (results.size() >= 10) break;
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
                            if (results.size() >= 10) break;
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
        result.setPageNo(pageNo);
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
