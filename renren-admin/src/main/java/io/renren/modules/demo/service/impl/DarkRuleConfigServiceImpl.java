package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.exception.RenException;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.modules.demo.dao.DarkRuleConfigDao;
import io.renren.modules.demo.dto.DarkRuleConfigDTO;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;
import io.renren.modules.demo.service.DarkRuleConfigService;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 检测规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Service
public class DarkRuleConfigServiceImpl extends CrudServiceImpl<DarkRuleConfigDao, DarkRuleConfigEntity, DarkRuleConfigDTO> implements DarkRuleConfigService {

    @Override
    public QueryWrapper<DarkRuleConfigEntity> getWrapper(Map<String, Object> params){
        String ruleName = (String)params.get("ruleName");
        String ruleCategory = (String)params.get("ruleCategory");
        String enabled = (String)params.get("enabled");

        QueryWrapper<DarkRuleConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(ruleName), "rule_name", ruleName);
        wrapper.eq(StrUtil.isNotBlank(ruleCategory), "rule_category", ruleCategory);
        wrapper.eq(StrUtil.isNotBlank(enabled), "enabled", enabled);
        wrapper.orderByAsc("sort_order");

        return wrapper;
    }

    @Override
    public void save(DarkRuleConfigDTO dto) {
        // 校验规则编码+参数键唯一性
        validateUnique(dto.getRuleCode(), dto.getParamKey(), null);
        super.save(dto);
    }

    @Override
    public void update(DarkRuleConfigDTO dto) {
        DarkRuleConfigEntity entity = baseDao.selectById(dto.getId());
        if (entity != null && "FIXED".equals(entity.getParamType())) {
            throw new RenException("固定值规则不可修改");
        }
        validateUnique(dto.getRuleCode(), dto.getParamKey(), dto.getId());
        super.update(dto);
    }

    @Override
    public void toggleEnabled(Long id) {
        DarkRuleConfigEntity entity = baseDao.selectById(id);
        if (entity == null) {
            throw new RenException("记录不存在");
        }
        if ("FIXED".equals(entity.getParamType())) {
            throw new RenException("固定值规则不可修改状态");
        }
        entity.setEnabled(entity.getEnabled() == 1 ? 0 : 1);
        updateById(entity);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            DarkRuleConfigEntity entity = baseDao.selectById(id);
            if (entity != null && "FIXED".equals(entity.getParamType())) {
                throw new RenException("固定值规则不可删除");
            }
        }
        super.delete(ids);
    }

    /**
     * 校验规则编码+参数键唯一性
     */
    private void validateUnique(String ruleCode, String paramKey, Long excludeId) {
        QueryWrapper<DarkRuleConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("rule_code", ruleCode);
        wrapper.eq("param_key", paramKey);
        wrapper.eq("del_flag", 0);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        Long count = baseDao.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new RenException("规则编码和参数键的组合已存在");
        }
    }
}