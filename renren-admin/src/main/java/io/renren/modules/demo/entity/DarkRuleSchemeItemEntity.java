package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 方案规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-26
 */
@Data
@TableName("t_dark_rule_scheme_item")
public class DarkRuleSchemeItemEntity extends BaseEntity {

    private Long schemeId;

    private Long ruleConfigId;

    private String paramValue;

    private Integer enabled;

    // 以下字段为关联查询字段，不存在于数据库表中
    @TableField(exist = false)
    private String ruleCode;

    @TableField(exist = false)
    private String ruleName;

    @TableField(exist = false)
    private String ruleCategory;
}