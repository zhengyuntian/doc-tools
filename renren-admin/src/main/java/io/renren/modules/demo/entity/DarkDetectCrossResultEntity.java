package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 关联分析结果表
 * 异步检测任务生成的数据，无审计字段（creator_name/updater_name/del_flag）
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_detect_cross_result")
public class DarkDetectCrossResultEntity {

    @TableId
    private Long id;

    /**
     * 批次ID
     */
	private Long batchId;
    /**
     * 分析类型：SENSITIVE_CROSS-敏感词跨文件合并，FORMAT_CONSISTENCY-格式一致性
     */
	private String analysisType;
    /**
     * 分析名称
     */
	private String analysisName;
    /**
     * 涉及的文件列表（JSON数组）
     */
	private String involvedFiles;
    /**
     * 实际检测值
     */
	private String actualValue;
    /**
     * 期望值
     */
	private String expectedValue;
    /**
     * 0-不通过，1-通过
     */
	private Integer isPass;
    /**
     * 1-警告，2-一般，3-严重
     */
	private Integer severity;
    /**
     * 修改建议
     */
	private String remark;
    /**
     * 创建时间（系统自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}