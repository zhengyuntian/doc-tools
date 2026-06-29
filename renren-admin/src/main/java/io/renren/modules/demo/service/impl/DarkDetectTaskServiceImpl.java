package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.demo.dao.DarkDetectBatchDao;
import io.renren.modules.demo.dao.DarkDetectResultDao;
import io.renren.modules.demo.dao.DarkDetectTaskDao;
import io.renren.modules.demo.dto.DarkDetectTaskDTO;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.entity.DarkDetectTaskEntity;
import io.renren.modules.demo.engine.DarkDetectEngine;
import io.renren.modules.demo.service.DarkDetectTaskService;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.security.user.UserDetail;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DarkDetectTaskServiceImpl extends CrudServiceImpl<DarkDetectTaskDao, DarkDetectTaskEntity, DarkDetectTaskDTO> implements DarkDetectTaskService {
    
    @Autowired
    private DarkDetectBatchDao darkDetectBatchDao;

    @Autowired
    private DarkDetectResultDao darkDetectResultDao;

    @Autowired
    private DarkDetectEngine darkDetectEngine;

    @Autowired
    private io.renren.modules.demo.engine.CrossFileAnalyzer crossFileAnalyzer;

    @Override
    public QueryWrapper<DarkDetectTaskEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String batchId = (String)params.get("batchId");
        String status = (String)params.get("status");
        String fileType = (String)params.get("fileType");
        String fileName = (String)params.get("fileName");
        String batchName = (String)params.get("batchName");

        QueryWrapper<DarkDetectTaskEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.eq(StrUtil.isNotBlank(batchId), "batch_id", batchId);
        wrapper.eq(StrUtil.isNotBlank(status), "status", status);
        wrapper.eq(StrUtil.isNotBlank(fileType), "file_type", fileType);
        wrapper.like(StrUtil.isNotBlank(fileName), "file_name", fileName);
        
        if (StrUtil.isNotBlank(batchName)) {
            QueryWrapper<DarkDetectBatchEntity> batchWrapper = new QueryWrapper<>();
            batchWrapper.eq("del_flag", 0);
            batchWrapper.like("batch_name", batchName);
            batchWrapper.select("id");
            List<Long> batchIds = darkDetectBatchDao.selectList(batchWrapper).stream()
                .map(DarkDetectBatchEntity::getId)
                .collect(Collectors.toList());
            if (!batchIds.isEmpty()) {
                wrapper.in("batch_id", batchIds);
            } else {
                wrapper.in("batch_id", -1);
            }
        }
        
        wrapper.orderByAsc("file_order");

        return wrapper;
    }

    @Override
    public PageData<DarkDetectTaskDTO> page(Map<String, Object> params) {
        PageData<DarkDetectTaskDTO> pageData = super.page(params);
        fillBatchName(pageData.getList());
        return pageData;
    }

    @Override
    public List<DarkDetectTaskDTO> list(Map<String, Object> params) {
        List<DarkDetectTaskDTO> dtoList = super.list(params);
        fillBatchName(dtoList);
        return dtoList;
    }

    private void fillBatchName(List<DarkDetectTaskDTO> dtoList) {
        for (DarkDetectTaskDTO dto : dtoList) {
            if (dto.getBatchId() != null) {
                DarkDetectBatchEntity batch = darkDetectBatchDao.selectById(dto.getBatchId());
                if (batch != null) {
                    dto.setBatchName(batch.getBatchName());
                }
            }
        }
    }

    @Override
    public List<DarkDetectTaskDTO> getByBatchId(Long batchId) {
        QueryWrapper<DarkDetectTaskEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_id", batchId);
        wrapper.orderByAsc("file_order");
        
        List<DarkDetectTaskEntity> entities = baseDao.selectList(wrapper);
        List<DarkDetectTaskDTO> dtoList = ConvertUtils.sourceToTarget(entities, DarkDetectTaskDTO.class);
        fillBatchName(dtoList);
        return dtoList;
    }

    @Override
    public void retry(Long id) {
        DarkDetectTaskEntity entity = baseDao.selectById(id);
        if (entity != null) {
            darkDetectResultDao.deleteByTaskId(entity.getId());
            
            entity.setStatus(1);
            entity.setTotalRules(0);
            entity.setPassRules(0);
            entity.setFailRules(0);
            entity.setErrorMsg(null);
            entity.setResultSummary(null);
            entity.setUpdateTime(new Date());
            updateById(entity);
            
            detectSingleTask(entity.getId(), entity.getBatchId());
        }
    }

    @Override
    public void startSingleTask(Long id) {
        DarkDetectTaskEntity entity = baseDao.selectById(id);
        if (entity != null && entity.getStatus() == 0) {
            darkDetectResultDao.deleteByTaskId(entity.getId());
            
            entity.setStatus(1);
            entity.setTotalRules(0);
            entity.setPassRules(0);
            entity.setFailRules(0);
            entity.setErrorMsg(null);
            entity.setResultSummary(null);
            entity.setCurrentRuleIndex(null);
            entity.setUpdateTime(new Date());
            updateById(entity);
            
            System.out.println("[暗标检测] 启动单任务检测，任务ID: " + id + ", 文件: " + entity.getFileName());
            
            detectSingleTask(entity.getId(), entity.getBatchId());
        }
    }

    @Override
    public void startBatchDetect(Long batchId) {
        UserDetail user = SecurityUser.getUser();
        String operatorName = user != null ? user.getUsername() : "系统";
        
        DarkDetectBatchEntity batchUpdate = new DarkDetectBatchEntity();
        batchUpdate.setId(batchId);
        batchUpdate.setStatus(3);
        darkDetectBatchDao.updateById(batchUpdate);
        
        QueryWrapper<DarkDetectTaskEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_id", batchId);
        
        List<DarkDetectTaskEntity> allTasks = baseDao.selectList(wrapper);
        
        for (DarkDetectTaskEntity task : allTasks) {
            darkDetectResultDao.deleteByTaskId(task.getId());
            task.setStatus(1);
            task.setTotalRules(0);
            task.setPassRules(0);
            task.setFailRules(0);
            task.setErrorMsg(null);
            task.setResultSummary(null);
            task.setCurrentRuleIndex(null);
            task.setCreatorName(operatorName);
            task.setUpdaterName(operatorName);
            task.setUpdateTime(new Date());
            updateById(task);
        }

        System.out.println("[暗标检测] 启动批次检测，批次ID: " + batchId + ", 任务数量: " + allTasks.size());

        processBatchDetection(batchId);
    }

    private void processBatchDetection(Long batchId) {
        new Thread(() -> {
            try {
                QueryWrapper<DarkDetectTaskEntity> taskWrapper = new QueryWrapper<>();
                taskWrapper.eq("batch_id", batchId);
                taskWrapper.eq("status", 1);

                List<DarkDetectTaskEntity> processingTasks = baseDao.selectList(taskWrapper);

                for (DarkDetectTaskEntity task : processingTasks) {
                    executeRealDetection(task);
                    Thread.sleep(500);
                }

                updateBatchStatus(batchId);
            } catch (Exception e) {
                e.printStackTrace();
                QueryWrapper<DarkDetectTaskEntity> failWrapper = new QueryWrapper<>();
                failWrapper.eq("batch_id", batchId);
                failWrapper.eq("status", 1);
                List<DarkDetectTaskEntity> failedTasks = baseDao.selectList(failWrapper);
                for (DarkDetectTaskEntity task : failedTasks) {
                    task.setStatus(3);
                    task.setErrorMsg("检测异常：" + e.getMessage());
                    task.setUpdateTime(new Date());
                    updateById(task);
                }
            }
        }).start();
    }

    private void detectSingleTask(Long taskId, Long batchId) {
        new Thread(() -> {
            try {
                DarkDetectTaskEntity task = baseDao.selectById(taskId);
                if (task == null || task.getStatus() != 1) {
                    System.out.println("[暗标检测] 任务不存在或状态不正确，跳过检测");
                    return;
                }

                executeRealDetection(task);
                updateBatchStatus(batchId);
            } catch (Exception e) {
                e.printStackTrace();
                DarkDetectTaskEntity task = baseDao.selectById(taskId);
                if (task != null) {
                    task.setStatus(3);
                    task.setErrorMsg("检测异常：" + e.getMessage());
                    task.setUpdateTime(new Date());
                    updateById(task);
                }
            }
        }).start();
    }

    private void executeRealDetection(DarkDetectTaskEntity task) {
        try {
            DarkDetectBatchEntity batch = darkDetectBatchDao.selectById(task.getBatchId());
            Long schemeId = batch != null ? batch.getSchemeId() : null;
            
            Integer schemeEnabled = batch != null ? batch.getSchemeEnabled() : task.getSchemeEnabled();
            Integer sensitiveEnabled = batch != null ? batch.getSensitiveEnabled() : task.getSensitiveEnabled();
            
            if (schemeEnabled == null) schemeEnabled = 1;
            if (sensitiveEnabled == null) sensitiveEnabled = 0;

            DarkDetectEngine.DetectResult result = darkDetectEngine.detect(
                    task.getId(),
                    task.getFilePath(),
                    task.getFileType(),
                    schemeId,
                    schemeEnabled,
                    sensitiveEnabled
            );

            for (DarkDetectResultEntity resultEntity : result.getResults()) {
                darkDetectResultDao.insert(resultEntity);
            }

            StringBuilder violations = new StringBuilder("[");
            for (int i = 0; i < result.getViolations().size(); i++) {
                if (i > 0) violations.append(",");
                violations.append("\"").append(result.getViolations().get(i)).append("\"");
            }
            violations.append("]");

            task.setStatus(2);
            task.setTotalRules(result.getTotalRules());
            task.setPassRules(result.getPassRules());
            task.setFailRules(result.getFailRules());
            task.setResultSummary("{\"violations\":" + violations.toString() + "}");
            task.setCurrentRuleIndex(null);
            task.setUpdateTime(new Date());
            updateById(task);

            System.out.println("[暗标检测] 任务ID: " + task.getId() + ", 文件: " + task.getFileName() + ", 检测完成, 通过: " + result.getPassRules() + "/" + result.getTotalRules());
        } catch (Exception e) {
            e.printStackTrace();
            task.setStatus(3);
            task.setErrorMsg("检测异常：" + e.getMessage());
            task.setUpdateTime(new Date());
            updateById(task);
        }
    }
    
    private void updateBatchStatus(Long batchId) {
        QueryWrapper<DarkDetectTaskEntity> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("batch_id", batchId);
        
        long total = baseDao.selectCount(taskWrapper);
        
        taskWrapper.eq("status", 2);
        long completed = baseDao.selectCount(taskWrapper);
        
        taskWrapper.clear();
        taskWrapper.eq("batch_id", batchId);
        taskWrapper.eq("status", 3);
        long failed = baseDao.selectCount(taskWrapper);
        
        taskWrapper.clear();
        taskWrapper.eq("batch_id", batchId);
        taskWrapper.eq("status", 1);
        long processing = baseDao.selectCount(taskWrapper);
        
        int status;
        boolean allCompleted = false;
        
        if (total == 0) {
            status = 0;
        } else if (processing > 0) {
            status = 3;
        } else if (completed + failed == total) {
            allCompleted = true;
            if (failed == 0) {
                status = 1;
            } else {
                status = 2;
            }
        } else {
            status = 0;
        }
        
        UpdateWrapper<DarkDetectBatchEntity> batchWrapper = new UpdateWrapper<>();
        batchWrapper.eq("id", batchId);
        batchWrapper.set("status", status);
        batchWrapper.set("total_files", (int) total);
        batchWrapper.set("pass_files", (int) completed);
        batchWrapper.set("fail_files", (int) failed);
        darkDetectBatchDao.update(null, batchWrapper);
        
        if (allCompleted) {
            crossFileAnalyzer.analyze(batchId);
        }
    }

    @Override
    public DarkDetectTaskDTO getTaskDetail(Long id) {
        return get(id);
    }
}
