package io.renren.modules.demo.dto;

import lombok.Data;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

@Data
@Schema(name = "方案规则配置表")
public class DarkRuleSchemeItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "配置项ID")
	private Long id;

	@SchemaProperty(name = "所属方案ID")
	private Long schemeId;

	@SchemaProperty(name = "关联规则配置ID")
	private Long ruleConfigId;

	@SchemaProperty(name = "用户设定的参数值")
	private String paramValue;

	@SchemaProperty(name = "是否启用该规则：0-禁用，1-启用")
	private Integer enabled;

	@SchemaProperty(name = "规则编码")
	private String ruleCode;

	@SchemaProperty(name = "规则名称")
	private String ruleName;

	@SchemaProperty(name = "规则分类")
	private String ruleCategory;

	@SchemaProperty(name = "参数键")
	private String paramKey;

	@SchemaProperty(name = "参数显示名称")
	private String paramName;

	@SchemaProperty(name = "参数类型")
	private String paramType;

	@SchemaProperty(name = "参数选项")
	private String paramOptions;

	@SchemaProperty(name = "单位")
	private String paramUnit;

	@SchemaProperty(name = "默认值")
	private String defaultValue;
}