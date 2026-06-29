package io.renren.modules.demo.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkSensitiveCategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词分类表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Mapper
public interface DarkSensitiveCategoryDao extends BaseDao<DarkSensitiveCategoryEntity> {
	
}