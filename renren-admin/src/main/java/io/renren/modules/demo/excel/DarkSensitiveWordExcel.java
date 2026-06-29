package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
public class DarkSensitiveWordExcel {
    @ExcelProperty(value = "敏感词ID")
    private Long id;
    @ExcelProperty(value = "所属分类ID（关联t_dark_sensitive_category.id）")
    private Long categoryId;
    @ExcelProperty(value = "敏感词内容")
    private String word;
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