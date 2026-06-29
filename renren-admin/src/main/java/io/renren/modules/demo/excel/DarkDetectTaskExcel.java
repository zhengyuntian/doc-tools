package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 暗标检测任务表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkDetectTaskExcel {
    @ExcelProperty(value = "任务ID")
    private Long id;
    @ExcelProperty(value = "所属批次ID")
    private Long batchId;
    @ExcelProperty(value = "任务编号（唯一）")
    private String taskNo;
    @ExcelProperty(value = "原始文件名")
    private String fileName;
    @ExcelProperty(value = "文件存储路径")
    private String filePath;
    @ExcelProperty(value = "文件大小（字节）")
    private Long fileSize;
    @ExcelProperty(value = "1-DOCX，2-PDF")
    private Integer fileType;
    @ExcelProperty(value = "文件在批次中的序号")
    private Integer fileOrder;
    @ExcelProperty(value = "0-排队中，1-检测中，2-已完成，3-失败")
    private Integer status;
    @ExcelProperty(value = "总检测规则数")
    private Integer totalRules;
    @ExcelProperty(value = "通过规则数")
    private Integer passRules;
    @ExcelProperty(value = "不通过规则数")
    private Integer failRules;
    @ExcelProperty(value = "检测结果摘要（JSON）")
    private String resultSummary;
    @ExcelProperty(value = "失败时的错误信息")
    private String errorMsg;
    @ExcelProperty(value = "创建人姓名")
    private String creatorName;
    @ExcelProperty(value = "更新人姓名")
    private String updaterName;
    @ExcelProperty(value = "创建时间")
    private Date createTime;
    @ExcelProperty(value = "更新时间")
    private Date updateTime;
    @ExcelProperty(value = "0-未删除，1-已删除")
    private Integer delFlag;

}