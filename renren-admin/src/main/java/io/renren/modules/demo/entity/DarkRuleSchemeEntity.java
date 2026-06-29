package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则方案表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-26
 */
@Data
@TableName("t_dark_rule_scheme")
public class DarkRuleSchemeEntity extends BaseEntity {

    private String schemeName;

    private String schemeDesc;

    private Integer isDefault;

    private Integer enabled;
}