package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public abstract class BaseEntity {

    @TableId
    private Long id;

	@TableField(value = "creator_name", fill = FieldFill.INSERT)
	private String creatorName;

	@TableField(value = "updater_name", fill = FieldFill.INSERT_UPDATE)
	private String updaterName;

	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;

	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;

	@TableField(value = "del_flag", fill = FieldFill.INSERT)
	private Integer delFlag;
}