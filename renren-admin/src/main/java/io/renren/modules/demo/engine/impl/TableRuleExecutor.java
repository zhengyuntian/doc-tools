package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedTable;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TableRuleExecutor implements RuleExecutor {

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
                    if (results.size() >= 10) break;
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
                    if (results.size() >= 10) break;
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
                    if (results.size() >= 10) break;
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
        return "table".equals(ruleCategory);
    }
}
