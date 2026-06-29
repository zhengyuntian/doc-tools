/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.common.utils.TreeNode;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 部门管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@Schema(title = "部门管理")
public class SysDeptDTO implements TreeNode, Serializable {
    private static final long serialVersionUID = 1L;

	@Schema(title = "id")
	@Null(message="{id.null}", groups = AddGroup.class)
	@NotNull(message="{id.require}", groups = UpdateGroup.class)
	private Long id;

	@Schema(title = "上级ID")
	@NotNull(message="{sysdept.pid.require}", groups = DefaultGroup.class)
	private Long pid;

	@Schema(title = "部门名称")
	@NotBlank(message="{sysdept.name.require}", groups = DefaultGroup.class)
	private String name;

	@Schema(title = "排序")
	@Min(value = 0, message = "{sort.number}", groups = DefaultGroup.class)
	private Integer sort;

	@Schema(title = "创建时间")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date createDate;

	@Schema(title = "上级部门名称")
	private String parentName;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	@Schema(title = "子部门")
	private List<SysDeptDTO> children;

	@Override
	@SuppressWarnings("unchecked")
	public List<SysDeptDTO> getChildren() {
		return children;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setChildren(List<?> list) {
		this.children = (List<SysDeptDTO>) list;
	}
}
