package io.renren.modules.demo.engine.impl;

import io.renren.modules.demo.engine.RuleExecutor;
import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedParagraph;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;
import io.renren.modules.demo.dao.DarkSensitiveWordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SensitiveRuleExecutor implements RuleExecutor {

    @Autowired
    private DarkSensitiveWordDao darkSensitiveWordDao;

    @Override
    public List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity ruleConfig) throws Exception {
        List<DarkDetectResultEntity> results = new ArrayList<>();
        String ruleCode = ruleConfig.getRuleCode();
        String ruleName = ruleConfig.getRuleName();

        if (!"SENSITIVE_WORD".equals(ruleCode)) {
            results.add(createPassResult(ruleCode, ruleName, "sensitive"));
            return results;
        }

        List<DarkSensitiveWordEntity> sensitiveWords = darkSensitiveWordDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DarkSensitiveWordEntity>()
                        .eq("enabled", 1)
        );

        if (sensitiveWords.isEmpty()) {
            results.add(createPassResult(ruleCode, ruleName, "sensitive"));
            return results;
        }

        boolean hasViolation = false;
        String fullText = document.getFullText();

        for (DarkSensitiveWordEntity word : sensitiveWords) {
            if (word.getWord() == null || word.getWord().isEmpty()) continue;

            Pattern pattern = Pattern.compile(Pattern.quote(word.getWord()));
            Matcher matcher = pattern.matcher(fullText);

            int matchCount = 0;
            while (matcher.find() && matchCount < 5) {
                int lineNum = 1;
                int charPos = matcher.start();
                int i = 0;
                while (i < charPos && i < fullText.length()) {
                    if (fullText.charAt(i) == '\n') {
                        lineNum++;
                    }
                    i++;
                }

                results.add(createResult(ruleCode, ruleName, "sensitive", lineNum, word.getWord(),
                        "检测到敏感词"));
                hasViolation = true;
                matchCount++;
            }
        }

        for (ParsedParagraph para : document.getParagraphs()) {
            for (DarkSensitiveWordEntity word : sensitiveWords) {
                if (word.getWord() == null) continue;
                if (para.getText() != null && para.getText().contains(word.getWord())) {
                    int logicalPageNo = document.getLogicalPageNo(para.getPageNo());
                    results.add(createResultWithPara(ruleCode, ruleName, "sensitive",
                            logicalPageNo, para.getIndex(), word.getWord(),
                            "段落" + (para.getIndex() + 1) + "检测到敏感词"));
                    hasViolation = true;
                    break;
                }
            }
        }

        if (!hasViolation) {
            results.add(createPassResult(ruleCode, ruleName, "sensitive"));
        }

        return results;
    }

    private DarkDetectResultEntity createResult(String ruleCode, String ruleName, String category,
                                                 int pageNo, String actual, String remark) {
        DarkDetectResultEntity result = new DarkDetectResultEntity();
        result.setRuleCode(ruleCode);
        result.setRuleName(ruleName);
        result.setRuleCategory(category);
        result.setPageNo(pageNo);
        result.setIsPass(0);
        result.setSeverity(3);
        result.setActualValue(actual);
        result.setExpectedValue("无敏感词");
        result.setRemark(remark);
        return result;
    }

    private DarkDetectResultEntity createResultWithPara(String ruleCode, String ruleName, String category,
                                                         int pageNo, int paraIndex, String actual, String remark) {
        DarkDetectResultEntity result = new DarkDetectResultEntity();
        result.setRuleCode(ruleCode);
        result.setRuleName(ruleName);
        result.setRuleCategory(category);
        result.setPageNo(pageNo);
        result.setParagraphIndex(paraIndex);
        result.setIsPass(0);
        result.setSeverity(3);
        result.setActualValue(actual);
        result.setExpectedValue("无敏感词");
        result.setRemark(remark);
        return result;
    }

    private DarkDetectResultEntity createPassResult(String ruleCode, String ruleName, String category) {
        DarkDetectResultEntity result = new DarkDetectResultEntity();
        result.setRuleCode(ruleCode);
        result.setRuleName(ruleName);
        result.setRuleCategory(category);
        result.setIsPass(1);
        result.setSeverity(1);
        result.setRemark("检测通过");
        return result;
    }

    @Override
    public boolean supports(String ruleCategory) {
        return "sensitive".equals(ruleCategory);
    }
}
