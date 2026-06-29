package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 单文件检测结果详情表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2026-06-24
 */
@Data
@TableName("t_dark_detect_result")
public class DarkDetectResultEntity {

    /**
     * 主键ID
     */
	private Long id;
    /**
     * 任务ID
     */
	private Long taskId;
    /**
     * 规则编码
     */
	private String ruleCode;
    /**
     * 规则名称
     */
	private String ruleName;
    /**
     * 规则分类：layout/font/table/sensitive
     */
	private String ruleCategory;
    /**
     * 检测类型：SCHEME-方案检测，SENSITIVE-敏感词检测
     */
	private String detectType;
    /**
     * 违规所在页码
     */
	private Integer pageNo;
    /**
     * 违规所在段落索引
     */
	private Integer paragraphIndex;
    /**
     * 违规起始偏移量
     */
	private Integer startOffset;
    /**
     * 违规结束偏移量
     */
	private Integer endOffset;
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
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标记（0-未删除，1-已删除）
     */
    private Integer delFlag;
}