package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
/**
 * 敏感词表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@Schema(name = "敏感词表")
public class DarkSensitiveWordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "敏感词ID")
	private Long id;

	@SchemaProperty(name = "所属分类ID（关联t_dark_sensitive_category.id）")
	private Long categoryId;

	@SchemaProperty(name = "所属分类名称")
	private String categoryName;

	@SchemaProperty(name = "敏感词内容")
	private String word;

	@SchemaProperty(name = "0-禁用，1-启用")
	private Integer enabled;

	@SchemaProperty(name = "创建人姓名")
	private String creatorName;

	@SchemaProperty(name = "更新人姓名")
	private String updaterName;

	@SchemaProperty(name = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@SchemaProperty(name = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	@SchemaProperty(name = "0-未删除，1-已删除")
	private Integer delFlag;


}
