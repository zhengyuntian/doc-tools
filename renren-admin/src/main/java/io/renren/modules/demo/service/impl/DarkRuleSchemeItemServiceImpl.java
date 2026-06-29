package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.demo.dao.DarkRuleSchemeItemDao;
import io.renren.modules.demo.dto.DarkRuleSchemeItemDTO;
import io.renren.modules.demo.entity.DarkRuleSchemeItemEntity;
import io.renren.modules.demo.service.DarkRuleSchemeItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DarkRuleSchemeItemServiceImpl extends CrudServiceImpl<DarkRuleSchemeItemDao, DarkRuleSchemeItemEntity, DarkRuleSchemeItemDTO> implements DarkRuleSchemeItemService {

    @Override
    public QueryWrapper<DarkRuleSchemeItemEntity> getWrapper(Map<String, Object> params){
        QueryWrapper<DarkRuleSchemeItemEntity> wrapper = new QueryWrapper<>();
        return wrapper;
    }

    @Override
    public List<DarkRuleSchemeItemDTO> listBySchemeId(Long schemeId) {
        List<DarkRuleSchemeItemEntity> entityList = baseDao.selectBySchemeIdWithConfig(schemeId);
        return ConvertUtils.sourceToTarget(entityList, currentDtoClass());
    }

    @Override
    public void deleteBySchemeId(Long schemeId) {
        // 物理删除方案下的所有规则项
        baseDao.deleteBySchemeIdPhysical(schemeId);
    }
}