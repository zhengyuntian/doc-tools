package io.renren.modules.demo.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.ExcelUtils;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.demo.dto.DarkDetectCrossResultDTO;
import io.renren.modules.demo.entity.DarkDetectCrossResultEntity;
import io.renren.modules.demo.excel.DarkDetectCrossResultExcel;
import io.renren.modules.demo.service.DarkDetectCrossResultService;
import io.renren.modules.demo.engine.CrossFileAnalyzer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo/darkdetectcrossresult")
@Tag(name="关联分析结果")
public class DarkDetectCrossResultController {
    @Autowired
    private DarkDetectCrossResultService darkDetectCrossResultService;

    @Autowired
    private CrossFileAnalyzer crossFileAnalyzer;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String"),
        @Parameter(name = "batchId", description = "批次ID", in = ParameterIn.QUERY, ref="Long"),
        @Parameter(name = "analysisType", description = "分析类型：SENSITIVE_CROSS/FORMAT_CONSISTENCY", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<PageData<DarkDetectCrossResultDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkDetectCrossResultDTO> page = darkDetectCrossResultService.page(params);
        return new Result<PageData<DarkDetectCrossResultDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<DarkDetectCrossResultDTO> get(@PathVariable("id") Long id){
        DarkDetectCrossResultDTO data = darkDetectCrossResultService.get(id);
        return new Result<DarkDetectCrossResultDTO>().ok(data);
    }

    @GetMapping("batch/{batchId}")
    @Operation(summary = "按批次查询分析结果")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<List<DarkDetectCrossResultDTO>> listByBatch(@PathVariable("batchId") Long batchId) {
        List<DarkDetectCrossResultDTO> list = darkDetectCrossResultService.listByBatchId(batchId);
        return new Result<List<DarkDetectCrossResultDTO>>().ok(list);
    }

    @PostMapping("analyze")
    @Operation(summary = "执行关联分析")
    @LogOperation("执行关联分析")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result analyze(@RequestBody Map<String, Long> request) {
        Long batchId = request.get("batchId");
        AssertUtils.isNull(batchId, "batchId");
        
        crossFileAnalyzer.analyze(batchId);
        return new Result();
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darkdetectcrossresult:save")
    public Result save(@RequestBody DarkDetectCrossResultDTO dto){
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        darkDetectCrossResultService.save(dto);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darkdetectcrossresult:update")
    public Result update(@RequestBody DarkDetectCrossResultDTO dto){
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);
        darkDetectCrossResultService.update(dto);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darkdetectcrossresult:delete")
    public Result delete(@RequestBody Long[] ids){
        AssertUtils.isArrayEmpty(ids, "id");
        darkDetectCrossResultService.delete(ids);
        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:darkdetectcrossresult:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DarkDetectCrossResultDTO> list = darkDetectCrossResultService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "关联分析结果表", list, DarkDetectCrossResultExcel.class);
    }
}
