package io.renren.modules.demo.engine;

import io.renren.modules.demo.entity.DarkDetectCrossResultEntity;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkDetectTaskEntity;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;
import io.renren.modules.demo.dao.DarkDetectResultDao;
import io.renren.modules.demo.dao.DarkDetectTaskDao;
import io.renren.modules.demo.dao.DarkDetectCrossResultDao;
import io.renren.modules.demo.dao.DarkSensitiveWordDao;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CrossFileAnalyzer {

    @Autowired
    private DarkDetectTaskDao darkDetectTaskDao;

    @Autowired
    private DarkDetectResultDao darkDetectResultDao;

    @Autowired
    private DarkDetectCrossResultDao darkDetectCrossResultDao;

    @Autowired
    private DarkSensitiveWordDao darkSensitiveWordDao;

    public void analyze(Long batchId) {
        System.out.println("[关联分析] 开始分析批次: " + batchId);

        darkDetectCrossResultDao.delete(new QueryWrapper<DarkDetectCrossResultEntity>()
                .eq("batch_id", batchId));

        List<DarkDetectTaskEntity> tasks = darkDetectTaskDao.selectList(
                new QueryWrapper<DarkDetectTaskEntity>()
                        .eq("batch_id", batchId)
                        .eq("status", 2)
        );

        if (tasks.isEmpty()) {
            System.out.println("[关联分析] 批次没有已完成的检测任务");
            return;
        }

        List<DarkDetectCrossResultEntity> crossResults = new ArrayList<>();

        crossResults.addAll(analyzeSensitiveWordCross(batchId, tasks));
        crossResults.addAll(analyzeFormatConsistency(batchId, tasks));

        for (DarkDetectCrossResultEntity result : crossResults) {
            darkDetectCrossResultDao.insert(result);
        }

        System.out.println("[关联分析] 分析完成，生成结果数: " + crossResults.size());
    }

    private List<DarkDetectCrossResultEntity> analyzeSensitiveWordCross(Long batchId, List<DarkDetectTaskEntity> tasks) {
        List<DarkDetectCrossResultEntity> results = new ArrayList<>();
        
        List<DarkSensitiveWordEntity> sensitiveWords = darkSensitiveWordDao.selectList(
                new QueryWrapper<DarkSensitiveWordEntity>().eq("enabled", 1)
        );

        if (sensitiveWords.isEmpty()) {
            return results;
        }

        Map<String, List<String>> wordFileMap = new LinkedHashMap<>();

        for (DarkDetectTaskEntity task : tasks) {
            List<DarkDetectResultEntity> taskResults = darkDetectResultDao.selectList(
                    new QueryWrapper<DarkDetectResultEntity>()
                            .eq("task_id", task.getId())
                            .eq("rule_category", "sensitive")
                            .eq("is_pass", 0)
            );

            for (DarkDetectResultEntity result : taskResults) {
                String word = result.getActualValue();
                if (word != null && !word.isEmpty()) {
                    wordFileMap.computeIfAbsent(word, k -> new ArrayList<>()).add(task.getFileName());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : wordFileMap.entrySet()) {
            String word = entry.getKey();
            List<String> files = entry.getValue();

            if (files.size() >= 2) {
                DarkDetectCrossResultEntity result = new DarkDetectCrossResultEntity();
                result.setBatchId(batchId);
                result.setAnalysisType("SENSITIVE_CROSS");
                result.setAnalysisName("敏感词跨文件重复");
                result.setInvolvedFiles(String.join(",", files));
                result.setActualValue(word + " (出现在 " + files.size() + " 个文件中)");
                result.setExpectedValue("敏感词不应在多个文件中重复出现");
                result.setIsPass(0);
                result.setSeverity(3);
                result.setRemark("敏感词 '" + word + "' 在以下文件中重复出现：" + String.join("、", files));
                results.add(result);
            }
        }

        if (wordFileMap.isEmpty()) {
            DarkDetectCrossResultEntity result = new DarkDetectCrossResultEntity();
            result.setBatchId(batchId);
            result.setAnalysisType("SENSITIVE_CROSS");
            result.setAnalysisName("敏感词跨文件检测");
            result.setInvolvedFiles("");
            result.setActualValue("无跨文件重复敏感词");
            result.setExpectedValue("无跨文件重复敏感词");
            result.setIsPass(1);
            result.setSeverity(1);
            result.setRemark("检测通过，未发现跨文件重复的敏感词");
            results.add(result);
        }

        return results;
    }

    private List<DarkDetectCrossResultEntity> analyzeFormatConsistency(Long batchId, List<DarkDetectTaskEntity> tasks) {
        List<DarkDetectCrossResultEntity> results = new ArrayList<>();

        Map<String, Map<String, String>> taskFormatMap = new LinkedHashMap<>();

        for (DarkDetectTaskEntity task : tasks) {
            List<DarkDetectResultEntity> taskResults = darkDetectResultDao.selectList(
                    new QueryWrapper<DarkDetectResultEntity>()
                            .eq("task_id", task.getId())
                            .eq("is_pass", 1)
            );

            Map<String, String> formatMap = new LinkedHashMap<>();
            for (DarkDetectResultEntity result : taskResults) {
                if (result.getExpectedValue() != null) {
                    formatMap.put(result.getRuleCode(), result.getExpectedValue());
                }
            }
            taskFormatMap.put(task.getFileName(), formatMap);
        }

        if (taskFormatMap.size() < 2) {
            return results;
        }

        Set<String> allRuleCodes = new LinkedHashSet<>();
        for (Map<String, String> formatMap : taskFormatMap.values()) {
            allRuleCodes.addAll(formatMap.keySet());
        }

        for (String ruleCode : allRuleCodes) {
            Map<String, List<String>> valueFilesMap = new LinkedHashMap<>();

            for (Map.Entry<String, Map<String, String>> entry : taskFormatMap.entrySet()) {
                String fileName = entry.getKey();
                String value = entry.getValue().get(ruleCode);
                if (value != null) {
                    valueFilesMap.computeIfAbsent(value, k -> new ArrayList<>()).add(fileName);
                }
            }

            if (valueFilesMap.size() > 1) {
                StringBuilder actualValue = new StringBuilder();
                StringBuilder remark = new StringBuilder("发现格式不一致：");
                
                for (Map.Entry<String, List<String>> valueEntry : valueFilesMap.entrySet()) {
                    if (actualValue.length() > 0) actualValue.append("; ");
                    actualValue.append(valueEntry.getKey()).append(" (").append(valueEntry.getValue().size()).append("个文件)");
                    remark.append(valueEntry.getKey()).append("[").append(String.join(",", valueEntry.getValue())).append("]; ");
                }

                DarkDetectCrossResultEntity result = new DarkDetectCrossResultEntity();
                result.setBatchId(batchId);
                result.setAnalysisType("FORMAT_CONSISTENCY");
                result.setAnalysisName("格式一致性检测-" + getRuleDisplayName(ruleCode));
                result.setInvolvedFiles(String.join(",", taskFormatMap.keySet()));
                result.setActualValue(actualValue.toString());
                result.setExpectedValue("所有文件格式一致");
                result.setIsPass(0);
                result.setSeverity(2);
                result.setRemark(remark.toString());
                results.add(result);
            }
        }

        if (results.isEmpty()) {
            DarkDetectCrossResultEntity result = new DarkDetectCrossResultEntity();
            result.setBatchId(batchId);
            result.setAnalysisType("FORMAT_CONSISTENCY");
            result.setAnalysisName("格式一致性检测");
            result.setInvolvedFiles(String.join(",", taskFormatMap.keySet()));
            result.setActualValue("所有文件格式一致");
            result.setExpectedValue("所有文件格式一致");
            result.setIsPass(1);
            result.setSeverity(1);
            result.setRemark("检测通过，所有文件格式保持一致");
            results.add(result);
        }

        return results;
    }

    private String getRuleDisplayName(String ruleCode) {
        Map<String, String> displayMap = new HashMap<>();
        displayMap.put("LAYOUT_PAPER_SIZE", "纸张大小");
        displayMap.put("LAYOUT_MARGIN_TOP", "上页边距");
        displayMap.put("LAYOUT_MARGIN_BOTTOM", "下页边距");
        displayMap.put("LAYOUT_MARGIN_LEFT", "左页边距");
        displayMap.put("LAYOUT_MARGIN_RIGHT", "右页边距");
        displayMap.put("FONT_FAMILY", "字体");
        displayMap.put("FONT_SIZE", "字号");
        displayMap.put("LAYOUT_LINE_SPACING", "行距");
        displayMap.put("LAYOUT_ALIGN", "对齐方式");
        displayMap.put("TABLE_BORDER", "表格边框");
        return displayMap.getOrDefault(ruleCode, ruleCode);
    }
}
