package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.renren.common.exception.RenException;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.modules.demo.dao.DarkDetectBatchDao;
import io.renren.modules.demo.dao.DarkDetectTaskDao;
import io.renren.modules.demo.dao.DarkRuleSchemeDao;
import io.renren.modules.demo.dto.DarkDetectBatchDTO;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;
import io.renren.modules.demo.entity.DarkDetectTaskEntity;
import io.renren.modules.demo.entity.DarkRuleSchemeEntity;
import io.renren.modules.demo.service.DarkDetectBatchService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DarkDetectBatchServiceImpl extends CrudServiceImpl<DarkDetectBatchDao, DarkDetectBatchEntity, DarkDetectBatchDTO> implements DarkDetectBatchService {

    @Autowired
    private DarkDetectTaskDao darkDetectTaskDao;

    @Autowired
    private DarkRuleSchemeDao darkRuleSchemeDao;

    @Override
    public PageData<DarkDetectBatchDTO> page(Map<String, Object> params) {
        PageData<DarkDetectBatchDTO> pageData = super.page(params);
        fillSchemeName(pageData.getList());
        return pageData;
    }

    private void fillSchemeName(List<DarkDetectBatchDTO> dtoList) {
        for (DarkDetectBatchDTO dto : dtoList) {
            if (dto.getSchemeId() != null) {
                DarkRuleSchemeEntity scheme = darkRuleSchemeDao.selectById(dto.getSchemeId());
                if (scheme != null) {
                    dto.setSchemeName(scheme.getSchemeName());
                }
            }
        }
    }

    @Override
    public QueryWrapper<DarkDetectBatchEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String batchName = (String)params.get("batchName");
        String status = (String)params.get("status");
        String startTime = (String)params.get("startTime");
        String endTime = (String)params.get("endTime");

        QueryWrapper<DarkDetectBatchEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(batchName), "batch_name", batchName);
        wrapper.eq(StrUtil.isNotBlank(status), "status", status);
        wrapper.ge(StrUtil.isNotBlank(startTime), "create_time", startTime);
        wrapper.le(StrUtil.isNotBlank(endTime), "create_time", endTime);
        wrapper.orderByDesc("create_time");

        return wrapper;
    }

    @Override
    public void save(DarkDetectBatchDTO dto) {
        String batchNo = "BATCH" + System.currentTimeMillis();
        dto.setBatchNo(batchNo);
        dto.setTotalFiles(0);
        dto.setPassFiles(0);
        dto.setFailFiles(0);
        dto.setErrorFiles(0);
        dto.setStatus(0);
        dto.setIsRelated(0);
        
        if (dto.getSchemeEnabled() == null) {
            dto.setSchemeEnabled(1);
        }
        if (dto.getSensitiveEnabled() == null) {
            dto.setSensitiveEnabled(0);
        }
        
        if (dto.getSchemeEnabled() == 0 && dto.getSensitiveEnabled() == 0) {
            throw new RenException("至少需要启用一种检测类型");
        }
        
        if (dto.getSchemeEnabled() == 1 && dto.getSchemeId() == null) {
            throw new RenException("启用方案检测时，必须选择检测方案");
        }
        
        super.save(dto);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            UpdateWrapper<DarkDetectTaskEntity> taskWrapper = new UpdateWrapper<>();
            taskWrapper.eq("batch_id", id);
            taskWrapper.set("del_flag", 1);
            darkDetectTaskDao.update(null, taskWrapper);
        }
        
        super.delete(ids);
    }

    @Override
    public Map<String, Object> getStatistics(Long batchId) {
        DarkDetectBatchEntity batch = baseDao.selectById(batchId);
        if (batch == null) {
            throw new RenException("批次不存在");
        }

        QueryWrapper<DarkDetectTaskEntity> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("batch_id", batchId);

        long total = darkDetectTaskDao.selectCount(taskWrapper);
        
        taskWrapper.eq("status", 2);
        long completed = darkDetectTaskDao.selectCount(taskWrapper);
        
        taskWrapper.clear();
        taskWrapper.eq("batch_id", batchId);
        taskWrapper.eq("status", 3);
        long failed = darkDetectTaskDao.selectCount(taskWrapper);
        
        taskWrapper.clear();
        taskWrapper.eq("batch_id", batchId);
        taskWrapper.eq("status", 0);
        long queued = darkDetectTaskDao.selectCount(taskWrapper);

        taskWrapper.clear();
        taskWrapper.eq("batch_id", batchId);
        taskWrapper.eq("status", 1);
        long processing = darkDetectTaskDao.selectCount(taskWrapper);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("batchId", batchId);
        statistics.put("batchName", batch.getBatchName());
        statistics.put("batchNo", batch.getBatchNo());
        statistics.put("totalFiles", total);
        statistics.put("completedFiles", completed);
        statistics.put("failedFiles", failed);
        statistics.put("queuedFiles", queued);
        statistics.put("processingFiles", processing);
        statistics.put("status", batch.getStatus());
        statistics.put("createTime", batch.getCreateTime());

        return statistics;
    }

    @Override
    public void updateBatchStatus(Long batchId) {
        Map<String, Object> stats = getStatistics(batchId);
        long total = (Long) stats.get("totalFiles");
        long completed = (Long) stats.get("completedFiles");
        long failed = (Long) stats.get("failedFiles");

        int status;
        if (total == 0) {
            status = 0;
        } else if (completed + failed == total) {
            if (failed == 0) {
                status = 1;
            } else {
                status = 2;
            }
        } else {
            status = 0;
        }

        DarkDetectBatchEntity batch = new DarkDetectBatchEntity();
        batch.setId(batchId);
        batch.setStatus(status);
        batch.setTotalFiles((int) total);
        batch.setPassFiles((int) completed);
        batch.setFailFiles((int) failed);
        updateById(batch);
    }
}
