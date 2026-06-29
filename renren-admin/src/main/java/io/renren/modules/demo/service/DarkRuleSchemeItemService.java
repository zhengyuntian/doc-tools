package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkRuleSchemeItemDTO;
import io.renren.modules.demo.entity.DarkRuleSchemeItemEntity;

import java.util.List;

/**
 * 方案规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-26
 */
public interface DarkRuleSchemeItemService extends CrudService<DarkRuleSchemeItemEntity, DarkRuleSchemeItemDTO> {

    List<DarkRuleSchemeItemDTO> listBySchemeId(Long schemeId);

    void deleteBySchemeId(Long schemeId);
}