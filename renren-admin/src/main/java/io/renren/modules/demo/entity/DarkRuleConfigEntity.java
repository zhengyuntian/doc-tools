package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 检测规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_rule_config")
public class DarkRuleConfigEntity extends BaseEntity {

    /**
     * 规则编码（固定值）
     */
	private String ruleCode;
    /**
     * 规则名称
     */
	private String ruleName;
    /**
     * 分类：layout/font/table/sensitive/cross
     */
	private String ruleCategory;
    /**
     * 规则分组标识（同一规则下的多个参数共享同一分组）
     */
	private String ruleGroup;
    /**
     * 参数键
     */
	private String paramKey;
    /**
     * 参数显示名称
     */
	private String paramName;
    /**
     * 参数值
     */
	private String paramValue;
    /**
     * 单位
     */
	private String paramUnit;
    /**
     * 参数类型：TEXT/NUMBER/FLOAT/SELECT/MULTI_SELECT/BOOLEAN/FIXED
     */
	private String paramType;
    /**
     * 可选选项列表（JSON数组）
     */
	private String paramOptions;
    /**
     * 排序
     */
	private Integer sortOrder;
    /**
     * 0-禁用，1-启用
     */
	private Integer enabled;
}