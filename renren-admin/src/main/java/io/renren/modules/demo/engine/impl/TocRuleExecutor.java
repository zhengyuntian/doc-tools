package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TocRuleExecutor implements RuleExecutor {
    
    private ParsedDocument document;

    @Override
    public List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity rule) {
        List<DarkDetectResultEntity> results = new ArrayList<>();
        String ruleCode = rule.getRuleCode();
        String ruleName = rule.getRuleName();
        String paramKey = rule.getParamKey();
        String expectedValue = rule.getParamValue();
        
        this.document = document;

        if (ruleCode == null || expectedValue == null) {
            return results;
        }

        if ("TOC_SETTING".equals(ruleCode)) {
            if ("hide_page_numbers".equals(paramKey)) {
                boolean hidePageNumbers = Boolean.parseBoolean(expectedValue);
                if (!hidePageNumbers) {
                    String fullText = document.getFullText();
                    if (fullText != null && fullText.contains("目录")) {
                        results.add(createResult(ruleCode, ruleName, "toc", 1, 1,
                                "显示页码", "隐藏页码", "目录显示了页码"));
                    }
                }
            } else if ("no_strikethrough".equals(paramKey)) {
                boolean noStrikethrough = Boolean.parseBoolean(expectedValue);
                if (noStrikethrough) {
                    String fullText = document.getFullText();
                    if (fullText != null && fullText.contains("目录")) {
                        results.add(createResult(ruleCode, ruleName, "toc", 1, 1,
                                "检查通过", "无删除线", "目录检查通过"));
                    }
                }
            }
        }

        if (results.isEmpty()) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setRuleCode(ruleCode);
            passResult.setRuleName(ruleName);
            passResult.setRuleCategory("toc");
            passResult.setIsPass(1);
            passResult.setRemark(ruleName + "通过检查");
            results.add(passResult);
        }

        return results;
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
        return "toc".equals(ruleCategory);
    }
}
