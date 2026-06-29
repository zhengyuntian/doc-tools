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
import io.renren.modules.demo.dao.DarkDetectResultDao;
import io.renren.modules.demo.dto.DarkDetectTaskDTO;
import io.renren.modules.demo.entity.DarkDetectResultEntity;
import io.renren.modules.demo.excel.DarkDetectTaskExcel;
import io.renren.modules.demo.service.DarkDetectTaskService;
import io.renren.modules.demo.util.DocxAnnotationUtils;
import io.renren.modules.demo.util.PdfAnnotationUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo/darkdetecttask")
@Tag(name="暗标检测任务管理")
public class DarkDetectTaskController {
    @Autowired
    private DarkDetectTaskService darkDetectTaskService;

    @Autowired
    private DarkDetectResultDao darkDetectResultDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String"),
        @Parameter(name = "batchId", description = "批次ID", in = ParameterIn.QUERY, ref="Long"),
        @Parameter(name = "status", description = "状态：0-排队中，1-检测中，2-已完成，3-失败", in = ParameterIn.QUERY, ref="Integer"),
        @Parameter(name = "fileType", description = "文件类型：1-DOCX，2-PDF", in = ParameterIn.QUERY, ref="Integer")
    })
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<PageData<DarkDetectTaskDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<DarkDetectTaskDTO> page = darkDetectTaskService.page(params);

        return new Result<PageData<DarkDetectTaskDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<DarkDetectTaskDTO> get(@PathVariable("id") Long id){
        DarkDetectTaskDTO data = darkDetectTaskService.get(id);

        return new Result<DarkDetectTaskDTO>().ok(data);
    }

    @GetMapping("{id}/detail")
    @Operation(summary = "任务详情")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<DarkDetectTaskDTO> detail(@PathVariable("id") Long id){
        DarkDetectTaskDTO data = darkDetectTaskService.getTaskDetail(id);

        return new Result<DarkDetectTaskDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result save(@RequestBody DarkDetectTaskDTO dto){
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        darkDetectTaskService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result update(@RequestBody DarkDetectTaskDTO dto){
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        darkDetectTaskService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result delete(@RequestBody Long[] ids){
        AssertUtils.isArrayEmpty(ids, "id");

        darkDetectTaskService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DarkDetectTaskDTO> list = darkDetectTaskService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "暗标检测任务表", list, DarkDetectTaskExcel.class);
    }

    @GetMapping("{id}/download")
    @Operation(summary = "下载文件")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
        DarkDetectTaskDTO task = darkDetectTaskService.get(id);
        if (task == null || task.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(task.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        File downloadFile = file;
        
        QueryWrapper<DarkDetectResultEntity> resultWrapper = new QueryWrapper<>();
        resultWrapper.eq("task_id", id);
        resultWrapper.eq("is_pass", 0);
        List<DarkDetectResultEntity> results = darkDetectResultDao.selectList(resultWrapper);
        
        if (!results.isEmpty()) {
            try {
                if (task.getFilePath().toLowerCase().endsWith(".docx")) {
                    downloadFile = DocxAnnotationUtils.addAnnotations(task.getFilePath(), results);
                } else if (task.getFilePath().toLowerCase().endsWith(".pdf")) {
                    downloadFile = PdfAnnotationUtils.addAnnotations(task.getFilePath(), results);
                }
            } catch (Exception e) {
                System.out.println("[下载文件] 添加标注失败: " + e.getMessage());
                e.printStackTrace();
                downloadFile = file;
            }
        }

        Resource resource = new FileSystemResource(downloadFile);
        
        String filename = URLEncoder.encode(task.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("{id}/retry")
    @Operation(summary = "重试任务")
    @LogOperation("重试任务")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result retry(@PathVariable("id") Long id) {
        darkDetectTaskService.retry(id);
        return new Result();
    }

    @PostMapping("{id}/start")
    @Operation(summary = "启动检测")
    @LogOperation("启动检测")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result start(@PathVariable("id") Long id) {
        darkDetectTaskService.startSingleTask(id);
        return new Result();
    }

    @GetMapping("batch/{batchId}")
    @Operation(summary = "按批次查询任务列表")
    @RequiresPermissions("demo:darkdetectbatch:page")
    public Result<List<DarkDetectTaskDTO>> listByBatch(@PathVariable("batchId") Long batchId) {
        List<DarkDetectTaskDTO> list = darkDetectTaskService.getByBatchId(batchId);
        return new Result<List<DarkDetectTaskDTO>>().ok(list);
    }
}
