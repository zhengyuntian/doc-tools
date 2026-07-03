package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
/**
 * 暗标检测任务表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@Schema(name = "暗标检测任务表")
public class DarkDetectTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "任务ID")
	private Long id;

	@SchemaProperty(name = "所属批次ID")
	private Long batchId;

	@SchemaProperty(name = "所属批次名称")
	private String batchName;

	@SchemaProperty(name = "是否启用方案检测：0-否，1-是")
	private Integer schemeEnabled;

	@SchemaProperty(name = "是否启用敏感词检测：0-否，1-是")
	private Integer sensitiveEnabled;

	@SchemaProperty(name = "任务编号（唯一）")
	private String taskNo;

	@SchemaProperty(name = "原始文件名")
	private String fileName;

	@SchemaProperty(name = "文件存储路径")
	private String filePath;

	@SchemaProperty(name = "文件大小（字节）")
	private Long fileSize;

	@SchemaProperty(name = "1-DOCX，2-PDF")
	private Integer fileType;

	@SchemaProperty(name = "文件在批次中的序号")
	private Integer fileOrder;

	@SchemaProperty(name = "0-排队中，1-检测中，2-已完成，3-失败")
	private Integer status;

	@SchemaProperty(name = "总检测规则数")
	private Integer totalRules;

	@SchemaProperty(name = "通过规则数")
	private Integer passRules;

	@SchemaProperty(name = "不通过规则数")
	private Integer failRules;

	@SchemaProperty(name = "检测结果摘要（JSON）")
	private String resultSummary;

	@SchemaProperty(name = "失败时的错误信息")
	private String errorMsg;

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

	@SchemaProperty(name = "0-未删除，1-已删除")
	private Integer delFlag;


}
