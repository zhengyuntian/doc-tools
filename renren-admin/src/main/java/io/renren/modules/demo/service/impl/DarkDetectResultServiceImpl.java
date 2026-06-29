package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.modules.demo.dao.DarkDetectResultDao;
import io.renren.modules.demo.dto.DarkDetectResultDTO;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.service.DarkDetectResultService;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 单文件检测结果详情表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Service
public class DarkDetectResultServiceImpl extends CrudServiceImpl<DarkDetectResultDao, DarkDetectResultEntity, DarkDetectResultDTO> implements DarkDetectResultService {

    @Override
    public QueryWrapper<DarkDetectResultEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<DarkDetectResultEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }


}