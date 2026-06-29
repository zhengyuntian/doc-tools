package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_sensitive_word")
public class DarkSensitiveWordEntity extends BaseEntity {

    /**
     * 所属分类ID（关联t_dark_sensitive_category.id）
     */
	private Long categoryId;
    /**
     * 所属分类名称（非数据库字段）
     */
	@TableField(exist = false)
	private String categoryName;
    /**
     * 敏感词内容
     */
	private String word;
    /**
     * 0-禁用，1-启用
     */
	private Integer enabled;
    }