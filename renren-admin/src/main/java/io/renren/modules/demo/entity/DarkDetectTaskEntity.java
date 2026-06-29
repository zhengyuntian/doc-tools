package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 暗标检测任务表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_detect_task")
public class DarkDetectTaskEntity extends BaseEntity {

    /**
     * 所属批次ID
     */
	private Long batchId;
    /**
     * 是否启用方案检测：0-否，1-是
     */
	private Integer schemeEnabled;
    /**
     * 是否启用敏感词检测：0-否，1-是
     */
	private Integer sensitiveEnabled;
    /**
     * 任务编号（唯一）
     */
	private String taskNo;
    /**
     * 原始文件名
     */
	private String fileName;
    /**
     * 文件存储路径
     */
	private String filePath;
    /**
     * 文件大小（字节）
     */
	private Long fileSize;
    /**
     * 1-DOCX，2-PDF
     */
	private Integer fileType;
    /**
     * 文件在批次中的序号
     */
	private Integer fileOrder;
    /**
     * 0-排队中，1-检测中，2-已完成，3-失败
     */
	private Integer status;
    /**
     * 总检测规则数
     */
	private Integer totalRules;
    /**
     * 当前检测规则索引（从0开始）
     */
	private Integer currentRuleIndex;
    /**
     * 通过规则数
     */
	private Integer passRules;
    /**
     * 不通过规则数
     */
	private Integer failRules;
    /**
     * 检测结果摘要（JSON）
     */
	private String resultSummary;
    /**
     * 失败时的错误信息
     */
	private String errorMsg;
}