package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;


/**
 * 暗标检测批次表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@Schema(name = "暗标检测批次表")
public class DarkDetectBatchDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "批次ID")
	private Long id;

	@SchemaProperty(name = "批次编号（唯一）")
	private String batchNo;

	@SchemaProperty(name = "批次名称")
	private String batchName;

	@SchemaProperty(name = "关联方案ID")
	private Long schemeId;

	@SchemaProperty(name = "关联方案名称")
	private String schemeName;

	@SchemaProperty(name = "是否启用方案检测：0-否，1-是")
	private Integer schemeEnabled;

	@SchemaProperty(name = "是否启用敏感词检测：0-否，1-是")
	private Integer sensitiveEnabled;

	@SchemaProperty(name = "总文件数")
	private Integer totalFiles;

	@SchemaProperty(name = "全部通过的文件数")
	private Integer passFiles;

	@SchemaProperty(name = "存在违规的文件数")
	private Integer failFiles;

	@SchemaProperty(name = "检测失败的文件数")
	private Integer errorFiles;

	@SchemaProperty(name = "0-处理中，1-全部完成，2-部分失败")
	private Integer status;

	@SchemaProperty(name = "文件是否关联：0-否，1-是")
	private Integer isRelated;

	@SchemaProperty(name = "关联分析报告（JSON格式）")
	private String crossAnalysisResult;

	@SchemaProperty(name = "批次结果摘要（JSON）")
	private String resultSummary;

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
