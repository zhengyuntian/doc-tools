package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.common.page.PageData;
import io.renren.modules.demo.dto.DarkSensitiveWordDTO;
import io.renren.modules.demo.entity.DarkSensitiveWordEntity;

import java.util.Map;

/**
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
public interface DarkSensitiveWordService extends CrudService<DarkSensitiveWordEntity, DarkSensitiveWordDTO> {

    PageData<DarkSensitiveWordDTO> pageByCategory(Long categoryId, Map<String, Object> params);
}