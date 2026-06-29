package io.renren.modules.demo.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.demo.dao.DarkDetectTaskDao;
import io.renren.modules.demo.dto.DarkDetectBatchDTO;
import io.renren.modules.demo.dto.DarkDetectTaskDTO;
import io.renren.modules.demo.entity.DarkDetectBatchEntity;
import io.renren.modules.demo.service.DarkDetectBatchService;
import io.renren.modules.demo.service.DarkDetectTaskService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("demo/darkdetectbatch")
@Tag(name="暗标检测批次管理")
public class DarkDetectBatchController {
    @Autowired
    private DarkDetectBatchService darkDetectBatchService;
    @Autowired
    private DarkDetectTaskService darkDetectTaskService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String"),
        @Parameter(name = "batchName", description = "批次名称", in = ParameterIn.QUERY, ref="String"),
        @Parameter(name = "status", description = "状态：0-处理中，1-全部完成，2-部分失败", in = ParameterIn.QUERY, ref="Integer"),
        @Parameter(name = "startTime", description = "开始时间", in = ParameterIn.QUERY, ref="String"),
        @Parameter(name = "endTime", description = "结束时间", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<PageData<DarkDetectBatchDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkDetectBatchDTO> page = darkDetectBatchService.page(params);

        return new Result<PageData<DarkDetectBatchDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darkdetectbatch:info")
    public Result<DarkDetectBatchDTO> get(@PathVariable("id") Long id){
        DarkDetectBatchDTO data = darkDetectBatchService.get(id);

        return new Result<DarkDetectBatchDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darkdetectbatch:save")
    public Result save(@RequestBody DarkDetectBatchDTO dto){
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        darkDetectBatchService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darkdetectbatch:update")
    public Result update(@RequestBody DarkDetectBatchDTO dto){
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        darkDetectBatchService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darkdetectbatch:delete")
    public Result delete(@RequestBody Long[] ids){
        AssertUtils.isArrayEmpty(ids, "id");

        darkDetectBatchService.delete(ids);

        return new Result();
    }

    @PostMapping("{id}/upload")
    @Operation(summary = "上传文件")
    @LogOperation("上传文件")
    @RequiresPermissions("demo:darkdetectbatch:upload")
    public Result upload(@PathVariable("id") Long id, @RequestParam("files") MultipartFile[] files) throws IOException {
        DarkDetectBatchDTO batch = darkDetectBatchService.get(id);
        if (batch == null) {
            return new Result().error("批次不存在");
        }

        String uploadDir = "/tmp/dark_upload/" + id + "/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        int count = 0;
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                continue;
            }

            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex).toLowerCase();
            }

            int fileType = 0;
            if (extension.equals(".docx")) {
                fileType = 1;
            } else if (extension.equals(".pdf")) {
                fileType = 2;
            } else {
                continue;
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + newFilename;
            file.transferTo(new File(filePath));

            DarkDetectTaskDTO taskDTO = new DarkDetectTaskDTO();
            taskDTO.setBatchId(id);
            taskDTO.setSchemeEnabled(batch.getSchemeEnabled());
            taskDTO.setSensitiveEnabled(batch.getSensitiveEnabled());
            taskDTO.setTaskNo("TASK" + System.currentTimeMillis() + count);
            taskDTO.setFileName(originalFilename);
            taskDTO.setFilePath(filePath);
            taskDTO.setFileSize(file.getSize());
            taskDTO.setFileType(fileType);
            taskDTO.setFileOrder(count + 1);
            taskDTO.setStatus(0);
            taskDTO.setTotalRules(0);
            taskDTO.setPassRules(0);
            taskDTO.setFailRules(0);

            darkDetectTaskService.save(taskDTO);
            count++;
        }

        darkDetectBatchService.updateBatchStatus(id);

        return new Result().ok(count + "个文件上传成功");
    }

    @PostMapping("{id}/detect")
    @Operation(summary = "启动检测")
    @LogOperation("启动检测")
    @RequiresPermissions("demo:darkdetectbatch:detect")
    public Result detect(@PathVariable("id") Long id) {
        DarkDetectBatchDTO batch = darkDetectBatchService.get(id);
        if (batch == null) {
            return new Result().error("批次不存在");
        }

        darkDetectTaskService.startBatchDetect(id);

        return new Result();
    }

    @GetMapping("{id}/statistics")
    @Operation(summary = "批次统计")
    @RequiresPermissions("demo:darkdetectbatch:info")
    public Result<Map<String, Object>> statistics(@PathVariable("id") Long id) {
        Map<String, Object> statistics = darkDetectBatchService.getStatistics(id);
        return new Result<Map<String, Object>>().ok(statistics);
    }
}
