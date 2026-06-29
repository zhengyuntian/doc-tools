package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 单文件检测结果详情表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkDetectResultExcel {
    @ExcelProperty(value = "结果ID")
    private Long id;
    @ExcelProperty(value = "任务ID")
    private Long taskId;
    @ExcelProperty(value = "规则编码")
    private String ruleCode;
    @ExcelProperty(value = "规则名称")
    private String ruleName;
    @ExcelProperty(value = "规则分类：layout/font/table/sensitive")
    private String ruleCategory;
    @ExcelProperty(value = "违规所在页码")
    private Integer pageNo;
    @ExcelProperty(value = "违规所在段落索引")
    private Integer paragraphIndex;
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