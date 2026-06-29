package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 暗标检测批次表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_detect_batch")
public class DarkDetectBatchEntity extends BaseEntity {

    /**
     * 批次编号（唯一）
     */
	private String batchNo;
    /**
     * 批次名称
     */
	private String batchName;
    /**
     * 关联方案ID
     */
	private Long schemeId;
    /**
     * 是否启用方案检测：0-否，1-是
     */
	private Integer schemeEnabled;
    /**
     * 是否启用敏感词检测：0-否，1-是
     */
	private Integer sensitiveEnabled;
    /**
     * 总文件数
     */
	private Integer totalFiles;
    /**
     * 全部通过的文件数
     */
	private Integer passFiles;
    /**
     * 存在违规的文件数
     */
	private Integer failFiles;
    /**
     * 检测失败的文件数
     */
	private Integer errorFiles;
    /**
     * 0-处理中，1-全部完成，2-部分失败
     */
	private Integer status;
    /**
     * 文件是否关联：0-否，1-是
     */
	private Integer isRelated;
    /**
     * 关联分析报告（JSON格式）
     */
	private String crossAnalysisResult;
    /**
     * 批次结果摘要（JSON）
     */
	private String resultSummary;
    }