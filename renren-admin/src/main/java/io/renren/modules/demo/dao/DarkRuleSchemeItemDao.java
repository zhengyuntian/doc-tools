package io.renren.modules.demo.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkRuleSchemeItemEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 方案规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-26
 */
@Mapper
public interface DarkRuleSchemeItemDao extends BaseDao<DarkRuleSchemeItemEntity> {

    @Select("SELECT i.*, c.rule_code as rule_code, c.rule_name as rule_name, c.rule_category as rule_category, " +
            "c.param_key as param_key, c.param_name as param_name, c.param_type as param_type, " +
            "c.param_options as param_options, c.param_unit as param_unit, c.param_value as default_value " +
            "FROM t_dark_rule_scheme_item i " +
            "LEFT JOIN t_dark_rule_config c ON i.rule_config_id = c.id AND c.del_flag = 0 " +
            "WHERE i.scheme_id = #{schemeId} AND i.del_flag = 0 " +
            "ORDER BY c.sort_order")
    List<DarkRuleSchemeItemEntity> selectBySchemeIdWithConfig(@Param("schemeId") Long schemeId);

    // 物理删除方案下的所有规则项
    @Delete("DELETE FROM t_dark_rule_scheme_item WHERE scheme_id = #{schemeId}")
    int deleteBySchemeIdPhysical(@Param("schemeId") Long schemeId);
}