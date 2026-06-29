package io.renren.modules.demo.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.dao.BaseDao;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 单文件检测结果详情表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Mapper
public interface DarkDetectResultDao extends BaseDao<DarkDetectResultEntity> {

    default int deleteByTaskId(@Param("taskId") Long taskId) {
        return delete(new QueryWrapper<DarkDetectResultEntity>().eq("task_id", taskId));
    }
}