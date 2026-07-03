package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;


/**
 * 关联分析结果表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@Schema(name = "关联分析结果表")
public class DarkDetectCrossResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "结果ID")
	private Long id;

	@SchemaProperty(name = "批次ID")
	private Long batchId;

	@SchemaProperty(name = "批次名称")
	private String batchName;

	@SchemaProperty(name = "分析类型：SENSITIVE_CROSS-敏感词跨文件合并，FORMAT_CONSISTENCY-格式一致性")
	private String analysisType;

	@SchemaProperty(name = "分析名称")
	private String analysisName;

	@SchemaProperty(name = "涉及的文件列表（JSON数组）")
	private String involvedFiles;

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
