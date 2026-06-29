package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 敏感词分类表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_sensitive_category")
public class DarkSensitiveCategoryEntity extends BaseEntity {

    /**
     * 分类名称（如：投标招标、联系方式）
     */
	private String categoryName;
    /**
     * 排序（前端展示顺序）
     */
	private Integer sortOrder;
    /**
     * 0-禁用，1-启用
     */
	private Integer enabled;
    /**
     * 备注
     */
	private String remark;
    }