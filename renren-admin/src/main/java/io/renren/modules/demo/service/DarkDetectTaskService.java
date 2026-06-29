package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DarkDetectTaskDTO;
import io.renren.modules.demo.entity.DarkDetectTaskEntity;

import java.util.List;

public interface DarkDetectTaskService extends CrudService<DarkDetectTaskEntity, DarkDetectTaskDTO> {

    List<DarkDetectTaskDTO> getByBatchId(Long batchId);

    void retry(Long id);

    void startSingleTask(Long id);

    void startBatchDetect(Long batchId);

    DarkDetectTaskDTO getTaskDetail(Long id);
}
