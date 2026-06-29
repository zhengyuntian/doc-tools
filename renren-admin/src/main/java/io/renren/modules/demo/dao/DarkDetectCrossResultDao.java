package io.renren.modules.demo.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkDetectCrossResultEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关联分析结果表
 * 异步任务表，不使用 BaseDao（无 del_flag 软删除字段）
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Mapper
public interface DarkDetectCrossResultDao extends BaseDao<DarkDetectCrossResultEntity> {
}