package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 关联分析结果表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkDetectCrossResultExcel {
    @ExcelProperty(value = "结果ID")
    private Long id;
    @ExcelProperty(value = "批次ID")
    private Long batchId;
    @ExcelProperty(value = "分析类型：SENSITIVE_CROSS-敏感词跨文件合并，FORMAT_CONSISTENCY-格式一致性")
    private String analysisType;
    @ExcelProperty(value = "分析名称")
    private String analysisName;
    @ExcelProperty(value = "涉及的文件列表（JSON数组）")
    private String involvedFiles;
    @ExcelProperty(value = "实际检测值")
    private String actualValue;
    @ExcelProperty(value = "期望值")
    private String expectedValue;
    @ExcelProperty(value = "0-不通过，1-通过")
    private Integer isPass;
    @ExcelProperty(value = "1-警告，2-一般，3-严重")
    private Integer severity;
    @ExcelProperty(value = "修改建议")
    private String remark;
    @ExcelProperty(value = "创建时间")
    private Date createTime;

}