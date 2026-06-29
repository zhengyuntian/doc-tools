package io.renren.modules.demo.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.demo.dao.*;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("demo/statistics")
@Tag(name = "暗标检测统计")
public class DarkStatisticsController {

    @Autowired
    private DarkDetectBatchDao darkDetectBatchDao;

    @Autowired
    private DarkDetectTaskDao darkDetectTaskDao;

    @Autowired
    private DarkSensitiveWordDao darkSensitiveWordDao;

    @Autowired
    private DarkRuleConfigDao darkRuleConfigDao;

    @Autowired
    private DarkRuleSchemeDao darkRuleSchemeDao;

    @GetMapping("overview")
    @Operation(summary = "系统概览统计")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();

        long totalBatch = darkDetectBatchDao.selectCount(null);
        result.put("totalBatch", totalBatch);

        long totalTask = darkDetectTaskDao.selectCount(null);
        result.put("totalTask", totalTask);

        long totalWord = darkSensitiveWordDao.selectCount(null);
        result.put("totalWord", totalWord);

        long totalRule = darkRuleConfigDao.selectCount(null);
        result.put("totalRule", totalRule);

        long totalScheme = darkRuleSchemeDao.selectCount(null);
        result.put("totalScheme", totalScheme);

        return new Result<Map<String, Object>>().ok(result);
    }

    @GetMapping("batchStatus")
    @Operation(summary = "批次状态统计")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<Map<String, Object>> batchStatus() {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<DarkDetectBatchEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DarkDetectBatchEntity::getStatus, 0);
        long processing = darkDetectBatchDao.selectCount(wrapper);
        result.put("processing", processing);

        wrapper.clear();
        wrapper.eq(DarkDetectBatchEntity::getStatus, 1);
        long completed = darkDetectBatchDao.selectCount(wrapper);
        result.put("completed", completed);

        wrapper.clear();
        wrapper.eq(DarkDetectBatchEntity::getStatus, 2);
        long failed = darkDetectBatchDao.selectCount(wrapper);
        result.put("failed", failed);

        return new Result<Map<String, Object>>().ok(result);
    }
}
