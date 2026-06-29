package io.renren.modules.demo.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Mapper
public interface DarkSensitiveWordDao extends BaseDao<DarkSensitiveWordEntity> {
	
	IPage<DarkSensitiveWordEntity> selectPageWithCategory(IPage<DarkSensitiveWordEntity> page, 
			@Param("word") String word, 
			@Param("categoryId") Long categoryId, 
			@Param("enabled") Integer enabled);
}