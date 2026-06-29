package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkRuleConfigDTO;
import io.renren.modules.demo.entity.DarkRuleConfigEntity;

/**
 * 检测规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
public interface DarkRuleConfigService extends CrudService<DarkRuleConfigEntity, DarkRuleConfigDTO> {

    /**
     * 切换启用/禁用状态
     * @param id 配置ID
     */
    void toggleEnabled(Long id);
}