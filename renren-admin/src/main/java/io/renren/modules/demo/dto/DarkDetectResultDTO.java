package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;


/**
 * 单文件检测结果详情表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@Schema(name = "单文件检测结果详情表")
public class DarkDetectResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "结果ID")
	private Long id;

	@SchemaProperty(name = "任务ID")
	private Long taskId;

	@SchemaProperty(name = "规则编码")
	private String ruleCode;

	@SchemaProperty(name = "规则名称")
	private String ruleName;

	@SchemaProperty(name = "规则分类：layout/font/table/sensitive")
	private String ruleCategory;

	@SchemaProperty(name = "违规所在页码")
	private Integer pageNo;

	@SchemaProperty(name = "违规所在段落索引")
	private Integer paragraphIndex;

	@SchemaProperty(name = "实际检测值")
	private String actualValue;

	@SchemaProperty(name = "期望值")
	private String expectedValue;

	@SchemaProperty(name = "0-不通过，1-通过")
	private Integer isPass;

	@SchemaProperty(name = "1-警告，2-一般，3-严重")
	private Integer severity;

	@SchemaProperty(name = "修改建议")
	private String remark;

	@SchemaProperty(name = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;


}
