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
import io.renren.modules.demo.dto.DarkSensitiveCategoryDTO;
import io.renren.modules.demo.excel.DarkSensitiveCategoryExcel;
import io.renren.modules.demo.service.DarkSensitiveCategoryService;
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
 * 敏感词分类表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@RestController
@RequestMapping("demo/darksensitivecategory")
@Tag(name="敏感词分类表")
public class DarkSensitiveCategoryController {
    @Autowired
    private DarkSensitiveCategoryService darkSensitiveCategoryService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("demo:darksensitivecategory:page")
    public Result<PageData<DarkSensitiveCategoryDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkSensitiveCategoryDTO> page = darkSensitiveCategoryService.page(params);

        return new Result<PageData<DarkSensitiveCategoryDTO>>().ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "获取所有分类")
    @RequiresPermissions("demo:darksensitivecategory:page")
    public Result<List<DarkSensitiveCategoryDTO>> list(){
        List<DarkSensitiveCategoryDTO> list = darkSensitiveCategoryService.listAll();

        return new Result<List<DarkSensitiveCategoryDTO>>().ok(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darksensitivecategory:info")
    public Result<DarkSensitiveCategoryDTO> get(@PathVariable("id") Long id){
        DarkSensitiveCategoryDTO data = darkSensitiveCategoryService.get(id);

        return new Result<DarkSensitiveCategoryDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darksensitivecategory:save")
    public Result save(@RequestBody DarkSensitiveCategoryDTO dto){
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        darkSensitiveCategoryService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darksensitivecategory:update")
    public Result update(@RequestBody DarkSensitiveCategoryDTO dto){
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        darkSensitiveCategoryService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darksensitivecategory:delete")
    public Result delete(@RequestBody Long[] ids){
        AssertUtils.isArrayEmpty(ids, "id");

        for (Long id : ids) {
            if (darkSensitiveCategoryService.hasRelatedWords(id)) {
                return new Result().error("分类下存在敏感词，无法删除");
            }
        }

        darkSensitiveCategoryService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:darksensitivecategory:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DarkSensitiveCategoryDTO> list = darkSensitiveCategoryService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "敏感词分类表", list, DarkSensitiveCategoryExcel.class);
    }

}
