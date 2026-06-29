package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkDetectCrossResultDTO;
import io.renren.modules.demo.entity.DarkDetectCrossResultEntity;

/**
 * 关联分析结果表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
public interface DarkDetectCrossResultService extends CrudService<DarkDetectCrossResultEntity, DarkDetectCrossResultDTO> {

    java.util.List<DarkDetectCrossResultDTO> listByBatchId(Long batchId);
}