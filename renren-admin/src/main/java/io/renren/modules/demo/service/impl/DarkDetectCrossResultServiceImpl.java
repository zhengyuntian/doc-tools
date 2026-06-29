package io.renren.modules.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.demo.dao.DarkDetectCrossResultDao;
import io.renren.modules.demo.dao.DarkDetectBatchDao;
import io.renren.modules.demo.dto.DarkDetectCrossResultDTO;
import io.renren.modules.demo.entity.DarkDetectCrossResultEntity;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;
import io.renren.modules.demo.service.DarkDetectCrossResultService;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DarkDetectCrossResultServiceImpl extends CrudServiceImpl<DarkDetectCrossResultDao, DarkDetectCrossResultEntity, DarkDetectCrossResultDTO> implements DarkDetectCrossResultService {

    @Autowired
    private DarkDetectBatchDao batchDao;

    @Override
    public QueryWrapper<DarkDetectCrossResultEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String batchId = (String)params.get("batchId");
        String analysisType = (String)params.get("analysisType");

        QueryWrapper<DarkDetectCrossResultEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.eq(StrUtil.isNotBlank(batchId), "batch_id", batchId);
        wrapper.eq(StrUtil.isNotBlank(analysisType), "analysis_type", analysisType);
        wrapper.orderByDesc("create_time");

        return wrapper;
    }

    @Override
    public PageData<DarkDetectCrossResultDTO> page(Map<String, Object> params) {
        IPage<DarkDetectCrossResultEntity> page = baseDao.selectPage(
            getPage(params, null, false),
            getWrapper(params)
        );

        PageData<DarkDetectCrossResultDTO> pageData = getPageData(page, currentDtoClass());
        
        // 填充批次名称
        fillBatchName(pageData.getList());
        
        return pageData;
    }

    @Override
    public List<DarkDetectCrossResultDTO> list(Map<String, Object> params) {
        List<DarkDetectCrossResultEntity> entityList = baseDao.selectList(getWrapper(params));
        List<DarkDetectCrossResultDTO> dtoList = ConvertUtils.sourceToTarget(entityList, currentDtoClass());
        
        // 填充批次名称
        fillBatchName(dtoList);
        
        return dtoList;
    }

    @Override
    public List<DarkDetectCrossResultDTO> listByBatchId(Long batchId) {
        QueryWrapper<DarkDetectCrossResultEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_id", batchId);
        wrapper.orderByDesc("create_time");
        
        List<DarkDetectCrossResultEntity> entities = baseDao.selectList(wrapper);
        List<DarkDetectCrossResultDTO> dtos = ConvertUtils.sourceToTarget(entities, DarkDetectCrossResultDTO.class);
        
        // 填充批次名称
        fillBatchName(dtos);
        
        return dtos;
    }

    /**
     * 填充批次名称
     */
    private void fillBatchName(List<DarkDetectCrossResultDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        // 收集所有批次ID
        Set<Long> batchIds = list.stream()
            .map(DarkDetectCrossResultDTO::getBatchId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        
        if (batchIds.isEmpty()) {
            return;
        }
        
        // 查询批次信息
        for (DarkDetectCrossResultDTO dto : list) {
            if (dto.getBatchId() != null) {
                DarkDetectBatchEntity batch = batchDao.selectById(dto.getBatchId());
                if (batch != null) {
                    dto.setBatchName(batch.getBatchName());
                }
            }
        }
    }
}