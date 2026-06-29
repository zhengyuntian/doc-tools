package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.demo.dao.DarkSensitiveCategoryDao;
import io.renren.modules.demo.dao.DarkSensitiveWordDao;
import io.renren.modules.demo.dto.DarkSensitiveCategoryDTO;
import io.renren.modules.demo.entity.DarkSensitiveCategoryEntity;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;
import io.renren.modules.demo.service.DarkSensitiveCategoryService;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 敏感词分类表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Service
public class DarkSensitiveCategoryServiceImpl extends CrudServiceImpl<DarkSensitiveCategoryDao, DarkSensitiveCategoryEntity, DarkSensitiveCategoryDTO> implements DarkSensitiveCategoryService {

    private final DarkSensitiveWordDao darkSensitiveWordDao;

    public DarkSensitiveCategoryServiceImpl(DarkSensitiveWordDao darkSensitiveWordDao) {
        this.darkSensitiveWordDao = darkSensitiveWordDao;
    }

    @Override
    public QueryWrapper<DarkSensitiveCategoryEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String categoryName = (String)params.get("categoryName");
        Integer enabled = params.get("enabled") != null ? Integer.parseInt(params.get("enabled").toString()) : null;

        QueryWrapper<DarkSensitiveCategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(categoryName), "category_name", categoryName);
        wrapper.eq(enabled != null, "enabled", enabled);
        wrapper.orderByAsc("sort_order");

        return wrapper;
    }

    @Override
    public List<DarkSensitiveCategoryDTO> listAll() {
        QueryWrapper<DarkSensitiveCategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        wrapper.orderByAsc("sort_order");
        List<DarkSensitiveCategoryEntity> entityList = baseDao.selectList(wrapper);
        return ConvertUtils.sourceToTarget(entityList, DarkSensitiveCategoryDTO.class);
    }

    @Override
    public boolean hasRelatedWords(Long categoryId) {
        QueryWrapper<DarkSensitiveWordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", categoryId);
        wrapper.eq("del_flag", 0);
        return darkSensitiveWordDao.selectCount(wrapper) > 0;
    }
}