package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 敏感词分类表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkSensitiveCategoryExcel {
    @ExcelProperty(value = "分类ID")
    private Long id;
    @ExcelProperty(value = "分类名称（如：投标招标、联系方式）")
    private String categoryName;
    @ExcelProperty(value = "排序（前端展示顺序）")
    private Integer sortOrder;
    @ExcelProperty(value = "0-禁用，1-启用")
    private Integer enabled;
    @ExcelProperty(value = "备注")
    private String remark;
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