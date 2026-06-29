package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkSensitiveCategoryDTO;
import io.renren.modules.demo.entity.DarkSensitiveCategoryEntity;

import java.util.List;

/**
 * 敏感词分类表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
public interface DarkSensitiveCategoryService extends CrudService<DarkSensitiveCategoryEntity, DarkSensitiveCategoryDTO> {

    List<DarkSensitiveCategoryDTO> listAll();

    boolean hasRelatedWords(Long categoryId);
}