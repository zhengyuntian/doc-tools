package io.renren.modules.demo.engine;

import io.renren.modules.demo.engine.model.ParsedDocument;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;

import java.util.List;

public interface RuleExecutor {
    List<DarkDetectResultEntity> execute(ParsedDocument document, DarkRuleConfigEntity ruleConfig) throws Exception;
    boolean supports(String ruleCategory);
}
