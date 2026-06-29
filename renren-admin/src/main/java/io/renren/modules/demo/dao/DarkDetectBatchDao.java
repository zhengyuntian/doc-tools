package io.renren.modules.demo.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 暗标检测批次表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Mapper
public interface DarkDetectBatchDao extends BaseDao<DarkDetectBatchEntity> {
	
}