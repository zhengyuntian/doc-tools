package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.exception.RenException;
import io.renren.common.exception.ErrorCode;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.page.PageData;
import io.renren.modules.demo.dao.DarkSensitiveWordDao;
import io.renren.modules.demo.dto.DarkSensitiveWordDTO;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;
import io.renren.modules.demo.service.DarkSensitiveWordService;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Service
public class DarkSensitiveWordServiceImpl extends CrudServiceImpl<DarkSensitiveWordDao, DarkSensitiveWordEntity, DarkSensitiveWordDTO> implements DarkSensitiveWordService {

    @Override
    public PageData<DarkSensitiveWordDTO> page(Map<String, Object> params) {
        String word = (String) params.get("word");
        Long categoryId = params.get("categoryId") != null ? Long.parseLong(params.get("categoryId").toString()) : null;
        Integer enabled = params.get("enabled") != null ? Integer.parseInt(params.get("enabled").toString()) : null;

        IPage<DarkSensitiveWordEntity> page = baseDao.selectPageWithCategory(
            getPage(params, null, false),
            word,
            categoryId,
            enabled
        );

        return getPageData(page, currentDtoClass());
    }

    @Override
    public QueryWrapper<DarkSensitiveWordEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String word = (String)params.get("word");
        Long categoryId = params.get("categoryId") != null ? Long.parseLong(params.get("categoryId").toString()) : null;
        Integer enabled = params.get("enabled") != null ? Integer.parseInt(params.get("enabled").toString()) : null;

        QueryWrapper<DarkSensitiveWordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(word), "word", word);
        wrapper.eq(categoryId != null, "category_id", categoryId);
        wrapper.eq(enabled != null, "enabled", enabled);
        wrapper.orderByAsc("create_time");

        return wrapper;
    }

    @Override
    public PageData<DarkSensitiveWordDTO> pageByCategory(Long categoryId, Map<String, Object> params) {
        params.put("categoryId", categoryId);
        return super.page(params);
    }

    @Override
    public void save(DarkSensitiveWordDTO dto) {
        // 校验是否存在未删除的同名敏感词
        validateUniqueWord(dto.getCategoryId(), dto.getWord(), null);
        super.save(dto);
    }

    @Override
    public void update(DarkSensitiveWordDTO dto) {
        // 校验是否存在未删除的同名敏感词（排除当前记录）
        validateUniqueWord(dto.getCategoryId(), dto.getWord(), dto.getId());
        super.update(dto);
    }

    /**
     * 校验敏感词唯一性
     * @param categoryId 分类ID
     * @param word 敏感词内容
     * @param excludeId 排除的记录ID（更新时使用）
     */
    private void validateUniqueWord(Long categoryId, String word, Long excludeId) {
        if (categoryId == null || StrUtil.isBlank(word)) {
            return;
        }
        
        QueryWrapper<DarkSensitiveWordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", categoryId);
        wrapper.eq("word", word);
        wrapper.eq("del_flag", 0);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        
        DarkSensitiveWordEntity existEntity = baseDao.selectOne(wrapper);
        if (existEntity != null) {
            throw new RenException(ErrorCode.DB_RECORD_EXISTS);
        }
    }
}