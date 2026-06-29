package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkRuleSchemeDTO;
import io.renren.modules.demo.entity.DarkRuleSchemeEntity;

import java.util.List;

/**
 * 规则方案表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-26
 */
public interface DarkRuleSchemeService extends CrudService<DarkRuleSchemeEntity, DarkRuleSchemeDTO> {

    DarkRuleSchemeDTO getDetail(Long id);

    void saveWithItems(DarkRuleSchemeDTO dto);

    void updateWithItems(DarkRuleSchemeDTO dto);

    void setDefault(Long id);

    List<DarkRuleSchemeDTO> listAll();
}