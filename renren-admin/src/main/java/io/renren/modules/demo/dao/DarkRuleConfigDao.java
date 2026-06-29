package io.renren.modules.demo.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检测规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Mapper
public interface DarkRuleConfigDao extends BaseDao<DarkRuleConfigEntity> {
	
}