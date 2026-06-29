package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 暗标检测批次表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkDetectBatchExcel {
    @ExcelProperty(value = "批次ID")
    private Long id;
    @ExcelProperty(value = "批次编号（唯一）")
    private String batchNo;
    @ExcelProperty(value = "批次名称")
    private String batchName;
    @ExcelProperty(value = "总文件数")
    private Integer totalFiles;
    @ExcelProperty(value = "全部通过的文件数")
    private Integer passFiles;
    @ExcelProperty(value = "存在违规的文件数")
    private Integer failFiles;
    @ExcelProperty(value = "检测失败的文件数")
    private Integer errorFiles;
    @ExcelProperty(value = "0-处理中，1-全部完成，2-部分失败")
    private Integer status;
    @ExcelProperty(value = "文件是否关联：0-否，1-是")
    private Integer isRelated;
    @ExcelProperty(value = "关联分析报告（JSON格式）")
    private String crossAnalysisResult;
    @ExcelProperty(value = "批次结果摘要（JSON）")
    private String resultSummary;
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