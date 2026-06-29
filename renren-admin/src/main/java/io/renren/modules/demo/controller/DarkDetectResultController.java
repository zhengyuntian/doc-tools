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
import io.renren.modules.demo.dto.DarkDetectResultDTO;
import io.renren.modules.demo.excel.DarkDetectResultExcel;
import io.renren.modules.demo.service.DarkDetectResultService;
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


/**
 * 单文件检测结果详情表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@RestController
@RequestMapping("demo/darkdetectresult")
@Tag(name="单文件检测结果详情表")
public class DarkDetectResultController {
    @Autowired
    private DarkDetectResultService darkDetectResultService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("demo:darkdetectresult:page")
    public Result<PageData<DarkDetectResultDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkDetectResultDTO> page = darkDetectResultService.page(params);

        return new Result<PageData<DarkDetectResultDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darkdetectresult:info")
    public Result<DarkDetectResultDTO> get(@PathVariable("id") Long id){
        DarkDetectResultDTO data = darkDetectResultService.get(id);

        return new Result<DarkDetectResultDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darkdetectresult:save")
    public Result save(@RequestBody DarkDetectResultDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        darkDetectResultService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darkdetectresult:update")
    public Result update(@RequestBody DarkDetectResultDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        darkDetectResultService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darkdetectresult:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        darkDetectResultService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:darkdetectresult:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DarkDetectResultDTO> list = darkDetectResultService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "单文件检测结果详情表", list, DarkDetectResultExcel.class);
    }

}
