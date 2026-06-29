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
import io.renren.modules.demo.dto.DarkSensitiveWordDTO;
import io.renren.modules.demo.excel.DarkSensitiveWordExcel;
import io.renren.modules.demo.service.DarkSensitiveWordService;
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
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@RestController
@RequestMapping("demo/darksensitiveword")
@Tag(name="敏感词表")
public class DarkSensitiveWordController {
    @Autowired
    private DarkSensitiveWordService darkSensitiveWordService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("demo:darksensitiveword:page")
    public Result<PageData<DarkSensitiveWordDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkSensitiveWordDTO> page = darkSensitiveWordService.page(params);

        return new Result<PageData<DarkSensitiveWordDTO>>().ok(page);
    }

    @GetMapping("category/{categoryId}")
    @Operation(summary = "按分类查询敏感词")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("demo:darksensitiveword:page")
    public Result<PageData<DarkSensitiveWordDTO>> pageByCategory(@PathVariable("categoryId") Long categoryId, @Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkSensitiveWordDTO> page = darkSensitiveWordService.pageByCategory(categoryId, params);

        return new Result<PageData<DarkSensitiveWordDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darksensitiveword:info")
    public Result<DarkSensitiveWordDTO> get(@PathVariable("id") Long id){
        DarkSensitiveWordDTO data = darkSensitiveWordService.get(id);

        return new Result<DarkSensitiveWordDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darksensitiveword:save")
    public Result save(@RequestBody DarkSensitiveWordDTO dto){
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        darkSensitiveWordService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darksensitiveword:update")
    public Result update(@RequestBody DarkSensitiveWordDTO dto){
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        darkSensitiveWordService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darksensitiveword:delete")
    public Result delete(@RequestBody Long[] ids){
        AssertUtils.isArrayEmpty(ids, "id");

        darkSensitiveWordService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:darksensitiveword:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DarkSensitiveWordDTO> list = darkSensitiveWordService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "敏感词表", list, DarkSensitiveWordExcel.class);
    }

}
