package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
/**
 * 检测规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@Schema(name = "检测规则配置表")
public class DarkRuleConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "配置ID")
	private Long id;

	@SchemaProperty(name = "规则编码（固定值）")
	private String ruleCode;

	@SchemaProperty(name = "规则名称")
	private String ruleName;

	@SchemaProperty(name = "分类：layout/font/table/sensitive/cross")
	private String ruleCategory;

	@SchemaProperty(name = "规则分组标识")
	private String ruleGroup;

	@SchemaProperty(name = "参数键")
	private String paramKey;

	@SchemaProperty(name = "参数显示名称")
	private String paramName;

	@SchemaProperty(name = "参数值")
	private String paramValue;

	@SchemaProperty(name = "单位")
	private String paramUnit;

	@SchemaProperty(name = "参数类型：TEXT/NUMBER/FLOAT/SELECT/MULTI_SELECT/BOOLEAN/FIXED")
	private String paramType;

	@SchemaProperty(name = "可选选项列表（JSON数组）")
	private String paramOptions;

	@SchemaProperty(name = "排序")
	private Integer sortOrder;

	@SchemaProperty(name = "0-禁用，1-启用")
	private Integer enabled;

	@SchemaProperty(name = "创建人姓名")
	private String creatorName;

	@SchemaProperty(name = "更新人姓名")
	private String updaterName;

	@SchemaProperty(name = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	@SchemaProperty(name = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	@SchemaProperty(name = "0-未删除，1-已删除")
	private Integer delFlag;


}
