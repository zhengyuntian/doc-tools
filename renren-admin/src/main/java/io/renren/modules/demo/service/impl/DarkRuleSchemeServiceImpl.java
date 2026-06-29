package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.exception.RenException;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.demo.dao.DarkRuleSchemeDao;
import io.renren.modules.demo.dto.DarkRuleSchemeDTO;
import io.renren.modules.demo.dto.DarkRuleSchemeItemDTO;
import io.renren.modules.demo.entity.DarkRuleSchemeEntity;
import io.renren.modules.demo.service.DarkRuleSchemeItemService;
import io.renren.modules.demo.service.DarkRuleSchemeService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DarkRuleSchemeServiceImpl extends CrudServiceImpl<DarkRuleSchemeDao, DarkRuleSchemeEntity, DarkRuleSchemeDTO> implements DarkRuleSchemeService {

    @Autowired
    private DarkRuleSchemeItemService darkRuleSchemeItemService;

    @Override
    public QueryWrapper<DarkRuleSchemeEntity> getWrapper(Map<String, Object> params){
        String schemeName = (String)params.get("schemeName");

        QueryWrapper<DarkRuleSchemeEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(schemeName), "scheme_name", schemeName);
        wrapper.orderByDesc("is_default");
        wrapper.orderByDesc("create_time");

        return wrapper;
    }

    @Override
    public DarkRuleSchemeDTO getDetail(Long id) {
        DarkRuleSchemeDTO dto = get(id);
        if (dto != null) {
            dto.setItems(darkRuleSchemeItemService.listBySchemeId(id));
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithItems(DarkRuleSchemeDTO dto) {
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault();
        }

        save(dto);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (DarkRuleSchemeItemDTO item : dto.getItems()) {
                item.setSchemeId(dto.getId());
                darkRuleSchemeItemService.save(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithItems(DarkRuleSchemeDTO dto) {
        update(dto);

        darkRuleSchemeItemService.deleteBySchemeId(dto.getId());

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (DarkRuleSchemeItemDTO item : dto.getItems()) {
                item.setId(null);
                item.setSchemeId(dto.getId());
                darkRuleSchemeItemService.save(item);
            }
        }

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault();
            DarkRuleSchemeEntity entity = baseDao.selectById(dto.getId());
            entity.setIsDefault(1);
            baseDao.updateById(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        DarkRuleSchemeEntity entity = baseDao.selectById(id);
        if (entity == null) {
            throw new RenException("方案不存在");
        }
        clearDefault();
        entity.setIsDefault(1);
        baseDao.updateById(entity);
    }

    private void clearDefault() {
        DarkRuleSchemeEntity defaultEntity = baseDao.selectOne(new QueryWrapper<DarkRuleSchemeEntity>().eq("is_default", 1));
        if (defaultEntity != null) {
            defaultEntity.setIsDefault(0);
            baseDao.updateById(defaultEntity);
        }
    }

    @Override
    public List<DarkRuleSchemeDTO> listAll() {
        List<DarkRuleSchemeEntity> entityList = baseDao.selectList(new QueryWrapper<DarkRuleSchemeEntity>().eq("del_flag", 0).orderByDesc("is_default").orderByDesc("create_time"));
        return ConvertUtils.sourceToTarget(entityList, DarkRuleSchemeDTO.class);
    }
}