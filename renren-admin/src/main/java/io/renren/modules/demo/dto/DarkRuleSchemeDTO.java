package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

@Data
@Schema(name = "规则方案表")
public class DarkRuleSchemeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "方案ID")
	private Long id;

	@SchemaProperty(name = "方案名称")
	private String schemeName;

	@SchemaProperty(name = "方案描述")
	private String schemeDesc;

	@SchemaProperty(name = "是否默认方案：0-否，1-是")
	private Integer isDefault;

	@SchemaProperty(name = "0-禁用，1-启用")
	private Integer enabled;

	@SchemaProperty(name = "创建人姓名")
	private String creatorName;

	@SchemaProperty(name = "更新人姓名")
	private String updaterName;

	@SchemaProperty(name = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	@SchemaProperty(name = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	@SchemaProperty(name = "方案包含的规则配置项")
	private List<DarkRuleSchemeItemDTO> items;
}