package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 检测规则配置表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkRuleConfigExcel {
    @ExcelProperty(value = "配置ID")
    private Long id;
    @ExcelProperty(value = "规则编码（固定值）")
    private String ruleCode;
    @ExcelProperty(value = "规则名称")
    private String ruleName;
    @ExcelProperty(value = "分类：layout/font/table/sensitive/cross")
    private String ruleCategory;
    @ExcelProperty(value = "参数键")
    private String paramKey;
    @ExcelProperty(value = "参数显示名称")
    private String paramName;
    @ExcelProperty(value = "参数值")
    private String paramValue;
    @ExcelProperty(value = "单位")
    private String paramUnit;
    @ExcelProperty(value = "排序")
    private Integer sortOrder;
    @ExcelProperty(value = "0-禁用，1-启用")
    private Integer enabled;
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