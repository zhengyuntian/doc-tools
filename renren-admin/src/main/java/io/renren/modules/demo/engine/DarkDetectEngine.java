package io.renren.modules.demo.engine;

import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.engine.model.ParsedParagraph;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import io.renren.modules.demo.entity.DarkRuleSchemeItemEntity;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;
import io.renren.modules.demo.dao.DarkRuleConfigDao;
import io.renren.modules.demo.dao.DarkRuleSchemeItemDao;
import io.renren.modules.demo.dao.DarkSensitiveWordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DarkDetectEngine {

    public static final String DETECT_TYPE_SCHEME = "SCHEME";
    public static final String DETECT_TYPE_SENSITIVE = "SENSITIVE";

    @Autowired
    private List<DocumentParser> documentParsers;

    @Autowired
    private List<RuleExecutor> ruleExecutors;

    @Autowired
    private DarkRuleSchemeItemDao darkRuleSchemeItemDao;

    @Autowired
    private DarkRuleConfigDao darkRuleConfigDao;

    @Autowired
    private DarkSensitiveWordDao darkSensitiveWordDao;

    public DetectResult detect(Long taskId, String filePath, Integer fileType, Long schemeId, 
                               Integer schemeEnabled, Integer sensitiveEnabled) throws Exception {
        System.out.println("[暗标检测] 开始检测，任务ID: " + taskId + ", 文件: " + filePath 
            + ", 方案ID: " + schemeId + ", schemeEnabled: " + schemeEnabled + ", sensitiveEnabled: " + sensitiveEnabled);

        DetectResult detectResult = new DetectResult();
        List<DarkDetectResultEntity> allResults = new ArrayList<>();
        List<String> allViolations = new ArrayList<>();
        int totalRules = 0;
        int passRules = 0;
        int failRules = 0;

        if (Boolean.TRUE.equals(schemeEnabled == 1)) {
            DetectResult schemeResult = detectByScheme(taskId, filePath, fileType, schemeId);
            allResults.addAll(schemeResult.getResults());
            allViolations.addAll(schemeResult.getViolations());
            totalRules += schemeResult.getTotalRules();
            passRules += schemeResult.getPassRules();
            failRules += schemeResult.getFailRules();
        }

        if (Boolean.TRUE.equals(sensitiveEnabled == 1)) {
            DetectResult sensitiveResult = detectBySensitiveWords(taskId, filePath, fileType);
            allResults.addAll(sensitiveResult.getResults());
            allViolations.addAll(sensitiveResult.getViolations());
            totalRules += sensitiveResult.getTotalRules();
            passRules += sensitiveResult.getPassRules();
            failRules += sensitiveResult.getFailRules();
        }

        detectResult.setResults(allResults);
        detectResult.setViolations(allViolations);
        detectResult.setTotalRules(totalRules);
        detectResult.setPassRules(passRules);
        detectResult.setFailRules(failRules);

        System.out.println("[暗标检测] 检测完成，总规则: " + totalRules + ", 通过: " + passRules + ", 失败: " + failRules);

        return detectResult;
    }

    public DetectResult detectByScheme(Long taskId, String filePath, Integer fileType, Long schemeId) throws Exception {
        System.out.println("[暗标检测] 开始检测，任务ID: " + taskId + ", 文件: " + filePath + ", 方案ID: " + schemeId);

        DocumentParser parser = getParser(fileType);
        if (parser == null) {
            throw new RuntimeException("不支持的文件类型: " + fileType);
        }

        ParsedDocument parsedDocument = parser.parse(filePath);
        System.out.println("[暗标检测] 文档解析完成，段落数: " + parsedDocument.getParagraphs().size() + ", 表格数: " + parsedDocument.getTables().size());

        List<DarkRuleConfigEntity> rules = loadRules(schemeId);
        System.out.println("[暗标检测] 加载规则数量: " + rules.size());

        DetectResult detectResult = new DetectResult();
        List<DarkDetectResultEntity> results = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        int passCount = 0;
        int failCount = 0;

        for (int i = 0; i < rules.size(); i++) {
            DarkRuleConfigEntity rule = rules.get(i);
            System.out.println("[暗标检测] 执行规则 " + (i + 1) + "/" + rules.size() + ": " + rule.getRuleName() 
                + ", category: " + rule.getRuleCategory() + ", paramValue: " + rule.getParamValue());

            RuleExecutor executor = getExecutor(rule.getRuleCategory());
            if (executor != null) {
                try {
                    List<DarkDetectResultEntity> ruleResults = executor.execute(parsedDocument, rule);
                    System.out.println("[暗标检测] 规则 " + rule.getRuleName() + " 执行结果数量: " + ruleResults.size());
                    
                    for (DarkDetectResultEntity r : ruleResults) {
                        r.setTaskId(taskId);
                        r.setDetectType(DETECT_TYPE_SCHEME);
                        results.add(r);

                        System.out.println("[暗标检测] 规则结果: ruleName=" + r.getRuleName() 
                            + ", isPass=" + r.getIsPass() + ", remark=" + r.getRemark());

                        if (r.getIsPass() == 1) {
                            passCount++;
                        } else {
                            failCount++;
                            violations.add(r.getRuleName() + "：" + r.getRemark());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[暗标检测] 规则执行异常: " + rule.getRuleName() + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                    DarkDetectResultEntity errorResult = new DarkDetectResultEntity();
                    errorResult.setTaskId(taskId);
                    errorResult.setRuleCode(rule.getRuleCode());
                    errorResult.setRuleName(rule.getRuleName());
                    errorResult.setRuleCategory(rule.getRuleCategory());
                    errorResult.setIsPass(0);
                    errorResult.setSeverity(3);
                    errorResult.setRemark("规则执行异常: " + e.getMessage());
                    results.add(errorResult);
                    failCount++;
                    violations.add(rule.getRuleName() + "：规则执行异常");
                }
            } else {
                System.out.println("[暗标检测] 未找到规则执行器: category=" + rule.getRuleCategory());
            }

            detectResult.setCurrentRuleIndex(i + 1);
            detectResult.setTotalRules(rules.size());
            detectResult.setPassRules(passCount);
            detectResult.setFailRules(failCount);

            Thread.sleep(100);
        }

        detectResult.setResults(results);
        detectResult.setViolations(violations);

        System.out.println("[暗标检测] 检测完成，总规则: " + rules.size() + ", 通过: " + passCount + ", 失败: " + failCount);

        return detectResult;
    }

    public DetectResult detectBySensitiveWords(Long taskId, String filePath, Integer fileType) throws Exception {
        System.out.println("[暗标检测] 开始敏感词检测，任务ID: " + taskId + ", 文件: " + filePath);

        DocumentParser parser = getParser(fileType);
        if (parser == null) {
            throw new RuntimeException("不支持的文件类型: " + fileType);
        }

        ParsedDocument parsedDocument = parser.parse(filePath);
        System.out.println("[暗标检测] 文档解析完成，段落数: " + parsedDocument.getParagraphs().size());

        List<DarkSensitiveWordEntity> sensitiveWords = darkSensitiveWordDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DarkSensitiveWordEntity>()
                        .eq("enabled", 1)
        );
        System.out.println("[暗标检测] 加载敏感词数量: " + sensitiveWords.size());

        DetectResult detectResult = new DetectResult();
        List<DarkDetectResultEntity> results = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        int passCount = 0;
        int failCount = 0;

        if (sensitiveWords.isEmpty()) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setTaskId(taskId);
            passResult.setRuleCode("SENSITIVE_WORD");
            passResult.setRuleName("敏感词检测");
            passResult.setRuleCategory("sensitive");
            passResult.setDetectType(DETECT_TYPE_SENSITIVE);
            passResult.setIsPass(1);
            passResult.setSeverity(1);
            passResult.setRemark("未配置敏感词，检测通过");
            results.add(passResult);
            passCount++;
            
            detectResult.setResults(results);
            detectResult.setViolations(violations);
            detectResult.setTotalRules(1);
            detectResult.setPassRules(passCount);
            detectResult.setFailRules(failCount);
            
            return detectResult;
        }

        boolean hasViolation = false;
        int maxResults = 50;

        for (ParsedParagraph para : parsedDocument.getParagraphs()) {
            if (results.size() >= maxResults) break;
            
            for (DarkSensitiveWordEntity word : sensitiveWords) {
                if (results.size() >= maxResults) break;
                if (word.getWord() == null || word.getWord().isEmpty()) continue;
                
                String paraText = para.getText();
                if (paraText == null || paraText.isEmpty()) continue;
                
                Pattern pattern = Pattern.compile(Pattern.quote(word.getWord()));
                Matcher matcher = pattern.matcher(paraText);
                
                while (matcher.find() && results.size() < maxResults) {
                    DarkDetectResultEntity result = new DarkDetectResultEntity();
                    result.setTaskId(taskId);
                    result.setRuleCode("SENSITIVE_WORD");
                    result.setRuleName("敏感词检测");
                    result.setRuleCategory("sensitive");
                    result.setDetectType(DETECT_TYPE_SENSITIVE);
                    result.setPageNo(para.getPageNo());
                    result.setParagraphIndex(para.getIndex());
                    result.setIsPass(0);
                    result.setSeverity(3);
                    result.setActualValue(word.getWord());
                    result.setExpectedValue("不应包含敏感词");
                    result.setRemark("检测到敏感词: \"" + word.getWord() + "\"");
                    results.add(result);
                    hasViolation = true;
                    failCount++;
                    violations.add("敏感词检测：页面" + para.getPageNo() + "段落" + (para.getIndex() + 1) + "检测到敏感词 '" + word.getWord() + "'");
                }
            }
        }

        if (!hasViolation) {
            DarkDetectResultEntity passResult = new DarkDetectResultEntity();
            passResult.setTaskId(taskId);
            passResult.setRuleCode("SENSITIVE_WORD");
            passResult.setRuleName("敏感词检测");
            passResult.setRuleCategory("sensitive");
            passResult.setDetectType(DETECT_TYPE_SENSITIVE);
            passResult.setIsPass(1);
            passResult.setSeverity(1);
            passResult.setRemark("未检测到敏感词");
            results.add(passResult);
            passCount++;
        }

        detectResult.setResults(results);
        detectResult.setViolations(violations);
        detectResult.setTotalRules(1);
        detectResult.setPassRules(passCount);
        detectResult.setFailRules(failCount);

        System.out.println("[暗标检测] 敏感词检测完成，通过: " + passCount + ", 失败: " + failCount);

        return detectResult;
    }

    private DocumentParser getParser(Integer fileType) {
        for (DocumentParser parser : documentParsers) {
            if (parser.supports(String.valueOf(fileType))) {
                return parser;
            }
        }
        return null;
    }

    private RuleExecutor getExecutor(String category) {
        for (RuleExecutor executor : ruleExecutors) {
            if (executor.supports(category)) {
                return executor;
            }
        }
        return null;
    }

    private List<DarkRuleConfigEntity> loadRules(Long schemeId) {
        if (schemeId != null) {
            List<DarkRuleSchemeItemEntity> schemeItems = darkRuleSchemeItemDao.selectBySchemeIdWithConfig(schemeId);
            
            Map<String, DarkRuleConfigEntity> ruleMap = new LinkedHashMap<>();
            
            for (DarkRuleSchemeItemEntity item : schemeItems) {
                if (item.getEnabled() == null || item.getEnabled() == 1) {
                    DarkRuleConfigEntity config = darkRuleConfigDao.selectById(item.getRuleConfigId());
                    if (config != null && (config.getEnabled() == null || config.getEnabled() == 1)) {
                        if (!ruleMap.containsKey(config.getRuleCode())) {
                            if (item.getParamValue() != null && !item.getParamValue().isEmpty()) {
                                config.setParamValue(item.getParamValue());
                            }
                            ruleMap.put(config.getRuleCode(), config);
                        }
                    }
                }
            }
            
            return new ArrayList<>(ruleMap.values());
        }

        return darkRuleConfigDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DarkRuleConfigEntity>()
                        .eq("enabled", 1)
                        .orderByAsc("sort_order")
        );
    }

    public static class DetectResult {
        private List<DarkDetectResultEntity> results;
        private List<String> violations;
        private int totalRules;
        private int passRules;
        private int failRules;
        private int currentRuleIndex;

        public List<DarkDetectResultEntity> getResults() { return results; }
        public void setResults(List<DarkDetectResultEntity> results) { this.results = results; }
        public List<String> getViolations() { return violations; }
        public void setViolations(List<String> violations) { this.violations = violations; }
        public int getTotalRules() { return totalRules; }
        public void setTotalRules(int totalRules) { this.totalRules = totalRules; }
        public int getPassRules() { return passRules; }
        public void setPassRules(int passRules) { this.passRules = passRules; }
        public int getFailRules() { return failRules; }
        public void setFailRules(int failRules) { this.failRules = failRules; }
        public int getCurrentRuleIndex() { return currentRuleIndex; }
        public void setCurrentRuleIndex(int currentRuleIndex) { this.currentRuleIndex = currentRuleIndex; }
    }
}
