package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkDetectBatchDTO;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;

import java.util.Map;

public interface DarkDetectBatchService extends CrudService<DarkDetectBatchEntity, DarkDetectBatchDTO> {

    Map<String, Object> getStatistics(Long batchId);

    void updateBatchStatus(Long batchId);
}
