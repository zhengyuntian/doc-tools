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
import io.renren.modules.demo.dto.DarkRuleSchemeDTO;
import io.renren.modules.demo.service.DarkRuleSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo/darkrulescheme")
@Tag(name = "规则方案管理")
public class DarkRuleSchemeController {
    @Autowired
    private DarkRuleSchemeService darkRuleSchemeService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("demo:darkrulescheme:page")
    public Result<PageData<DarkRuleSchemeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkRuleSchemeDTO> page = darkRuleSchemeService.page(params);

        return new Result<PageData<DarkRuleSchemeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darkrulescheme:info")
    public Result<DarkRuleSchemeDTO> info(@PathVariable("id") Long id){
        DarkRuleSchemeDTO dto = darkRuleSchemeService.getDetail(id);

        return new Result<DarkRuleSchemeDTO>().ok(dto);
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    @RequiresPermissions("demo:darkrulescheme:list")
    public Result<List<DarkRuleSchemeDTO>> list(){
        List<DarkRuleSchemeDTO> list = darkRuleSchemeService.listAll();

        return new Result<List<DarkRuleSchemeDTO>>().ok(list);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darkrulescheme:save")
    public Result save(@RequestBody DarkRuleSchemeDTO dto){
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        darkRuleSchemeService.saveWithItems(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darkrulescheme:update")
    public Result update(@RequestBody DarkRuleSchemeDTO dto){
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        darkRuleSchemeService.updateWithItems(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darkrulescheme:delete")
    public Result delete(@RequestBody Long[] ids){
        AssertUtils.isArrayEmpty(ids, "id");

        darkRuleSchemeService.delete(ids);

        return new Result();
    }

    @PostMapping("{id}/default")
    @Operation(summary = "设为默认方案")
    @LogOperation("设为默认方案")
    @RequiresPermissions("demo:darkrulescheme:update")
    public Result setDefault(@PathVariable("id") Long id){
        darkRuleSchemeService.setDefault(id);
        return new Result();
    }
}