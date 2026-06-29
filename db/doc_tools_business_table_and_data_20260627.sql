/*
 Navicat Premium Dump SQL

 Source Server         : 腾讯云
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 120.53.19.128:33060
 Source Schema         : doc_tools

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 27/06/2026 21:04:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_dark_detect_batch
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_detect_batch`;
CREATE TABLE `t_dark_detect_batch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `batch_no` varchar(32) NOT NULL COMMENT '批次编号（唯一）',
  `batch_name` varchar(200) DEFAULT NULL COMMENT '批次名称',
  `scheme_id` bigint(20) DEFAULT NULL COMMENT '关联方案ID',
  `total_files` int(11) NOT NULL DEFAULT '0' COMMENT '总文件数',
  `pass_files` int(11) NOT NULL DEFAULT '0' COMMENT '全部通过的文件数',
  `fail_files` int(11) NOT NULL DEFAULT '0' COMMENT '存在违规的文件数',
  `error_files` int(11) NOT NULL DEFAULT '0' COMMENT '检测失败的文件数',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-处理中，1-全部完成，2-部分失败',
  `is_related` tinyint(4) NOT NULL DEFAULT '0' COMMENT '文件是否关联：0-否，1-是',
  `cross_analysis_result` text COMMENT '关联分析报告（JSON格式）',
  `result_summary` text COMMENT '批次结果摘要（JSON）',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_creator_name` (`creator_name`),
  KEY `idx_status` (`status`),
  KEY `idx_batch_name` (`batch_name`(50)),
  KEY `idx_scheme_id` (`scheme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2070848272929570819 DEFAULT CHARSET=utf8mb4 COMMENT='暗标检测批次表';

-- ----------------------------
-- Records of t_dark_detect_batch
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_detect_batch` (`id`, `batch_no`, `batch_name`, `scheme_id`, `total_files`, `pass_files`, `fail_files`, `error_files`, `status`, `is_related`, `cross_analysis_result`, `result_summary`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070465719626321921, 'BATCH1782472469605', '第一批', NULL, 0, 0, 0, 0, 0, 1, NULL, NULL, '管理员', '管理员', '2026-06-26 19:14:30', '2026-06-26 19:14:30', 1);
INSERT INTO `t_dark_detect_batch` (`id`, `batch_no`, `batch_name`, `scheme_id`, `total_files`, `pass_files`, `fail_files`, `error_files`, `status`, `is_related`, `cross_analysis_result`, `result_summary`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070475175483371521, 'BATCH1782474724057', '第一批', 2070446135129063425, 8, 8, 0, 0, 1, 0, NULL, NULL, '管理员', '管理员', '2026-06-26 19:52:04', '2026-06-27 00:49:18', 1);
INSERT INTO `t_dark_detect_batch` (`id`, `batch_no`, `batch_name`, `scheme_id`, `total_files`, `pass_files`, `fail_files`, `error_files`, `status`, `is_related`, `cross_analysis_result`, `result_summary`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070846121297760257, 'BATCH1782563164431', '第一批', 2070446135129063425, 3, 3, 0, 0, 1, 0, NULL, NULL, '管理员', '管理员', '2026-06-26 19:52:04', '2026-06-27 12:33:42', 1);
INSERT INTO `t_dark_detect_batch` (`id`, `batch_no`, `batch_name`, `scheme_id`, `total_files`, `pass_files`, `fail_files`, `error_files`, `status`, `is_related`, `cross_analysis_result`, `result_summary`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070848272929570818, 'BATCH1782563677420', '第一批', 2070446135129063425, 1, 1, 0, 0, 1, 0, NULL, NULL, '管理员', NULL, '2026-06-27 20:34:37', '2026-06-27 12:35:13', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_detect_cross_result
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_detect_cross_result`;
CREATE TABLE `t_dark_detect_cross_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `analysis_type` varchar(50) NOT NULL COMMENT '分析类型：SENSITIVE_CROSS-敏感词跨文件合并，FORMAT_CONSISTENCY-格式一致性',
  `analysis_name` varchar(100) NOT NULL COMMENT '分析名称',
  `involved_files` text COMMENT '涉及的文件列表（JSON数组）',
  `actual_value` text COMMENT '实际检测值',
  `expected_value` varchar(500) DEFAULT NULL COMMENT '期望值',
  `is_pass` tinyint(4) NOT NULL COMMENT '0-不通过，1-通过',
  `severity` tinyint(4) DEFAULT '1' COMMENT '1-警告，2-一般，3-严重',
  `remark` varchar(500) DEFAULT NULL COMMENT '修改建议',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_analysis_type` (`analysis_type`),
  KEY `idx_is_pass` (`is_pass`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关联分析结果表';

-- ----------------------------
-- Records of t_dark_detect_cross_result
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for t_dark_detect_result
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_detect_result`;
CREATE TABLE `t_dark_detect_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_category` varchar(50) DEFAULT NULL COMMENT '规则分类：layout/font/table/sensitive',
  `page_no` int(11) DEFAULT NULL COMMENT '违规所在页码',
  `paragraph_index` int(11) DEFAULT NULL COMMENT '违规所在段落索引',
  `start_offset` int(11) DEFAULT NULL COMMENT '违规起始偏移量',
  `end_offset` int(11) DEFAULT NULL COMMENT '违规结束偏移量',
  `actual_value` varchar(500) DEFAULT NULL COMMENT '实际检测值',
  `expected_value` varchar(500) DEFAULT NULL COMMENT '期望值',
  `is_pass` tinyint(4) NOT NULL COMMENT '0-不通过，1-通过',
  `severity` tinyint(4) DEFAULT '1' COMMENT '1-警告，2-一般，3-严重',
  `remark` varchar(500) DEFAULT NULL COMMENT '修改建议',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_rule_code` (`rule_code`),
  KEY `idx_is_pass` (`is_pass`)
) ENGINE=InnoDB AUTO_INCREMENT=2070848401921196034 DEFAULT CHARSET=utf8mb4 COMMENT='单文件检测结果详情表';

-- ----------------------------
-- Records of t_dark_detect_result
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458001063937, 2070491446895681538, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '2cm', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：2cm，期望值：标准值', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458143670274, 2070491446895681538, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458311442433, 2070491446895681538, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458454048769, 2070491446895681538, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458592460802, 2070491446895681538, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458735067138, 2070491446895681538, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842458873479170, 2070491446895681538, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:31', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842477567492098, 2070491447067648002, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '左对齐', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：左对齐，期望值：标准值', '2026-06-27 12:11:35', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842477718487041, 2070491447067648002, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:35', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842477882064898, 2070491447067648002, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:35', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842478024671234, 2070491447067648002, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:35', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842478163083266, 2070491447067648002, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:35', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842478309883905, 2070491447067648002, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:35', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842478456684546, 2070491447067648002, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:36', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842496924205057, 2070491515782930433, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '14号', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：14号，期望值：标准值', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842497062617090, 2070491515782930433, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842497196834818, 2070491515782930433, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842497335246849, 2070491515782930433, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842497473658882, 2070491515782930433, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842497620459522, 2070491515782930433, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842497767260162, 2070491515782930433, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:40', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842517132361729, 2070491515925536769, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '加粗', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：加粗，期望值：标准值', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842517279162369, 2070491515925536769, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842517430157314, 2070491515925536769, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842517576957954, 2070491515925536769, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842517715369986, 2070491515925536769, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842517878947842, 2070491515925536769, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842518021554178, 2070491515925536769, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:45', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842536581349377, 2070495799031328770, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '加粗', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：加粗，期望值：标准值', '2026-06-27 12:11:49', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842536728150017, 2070495799031328770, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:49', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842536870756353, 2070495799031328770, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:49', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842537021751298, 2070495799031328770, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:49', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842537168551937, 2070495799031328770, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:50', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842537319546881, 2070495799031328770, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:50', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842537466347522, 2070495799031328770, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:50', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556131000321, 2070495799211683842, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '14号', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：14号，期望值：标准值', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556273606657, 2070495799211683842, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556416212994, 2070495799211683842, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556563013634, 2070495799211683842, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556709814273, 2070495799211683842, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556852420609, 2070495799211683842, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842556995026946, 2070495799211683842, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:54', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842575508684801, 2070495799769526273, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '宋体', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：宋体，期望值：标准值', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842575651291137, 2070495799769526273, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842575793897474, 2070495799769526273, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842575932309505, 2070495799769526273, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842576095887362, 2070495799769526273, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842576234299394, 2070495799769526273, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842576494346242, 2070495799769526273, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:11:59', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842594991226881, 2070495799966658562, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '宋体', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：宋体，期望值：标准值', '2026-06-27 12:12:03', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842595125444610, 2070495799966658562, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:12:03', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842595259662337, 2070495799966658562, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:12:03', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842595398074369, 2070495799966658562, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:12:03', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842595536486402, 2070495799966658562, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:12:03', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842595679092737, 2070495799966658562, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:12:03', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070842595817504770, 2070495799966658562, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:12:04', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847981396082689, 2070846233482809346, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '单倍行距', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：单倍行距，期望值：标准值', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847981547077633, 2070846233482809346, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847981693878273, 2070846233482809346, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847981857456129, 2070846233482809346, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847982029422593, 2070846233482809346, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847982205583362, 2070846233482809346, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070847982385938434, 2070846233482809346, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:32', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848001084145666, 2070846233608638465, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '左对齐', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：左对齐，期望值：标准值', '2026-06-27 12:33:36', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848001247723521, 2070846233608638465, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:36', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848001407107074, 2070846233608638465, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:36', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848001562296321, 2070846233608638465, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:36', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848001713291265, 2070846233608638465, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:36', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848001864286210, 2070846233608638465, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:37', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848002023669761, 2070846233608638465, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:37', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848020776402946, 2070846233742856194, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '12号', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：12号，期望值：标准值', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848020939980801, 2070846233742856194, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848021103558658, 2070846233742856194, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848021258747906, 2070846233742856194, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848021409742849, 2070846233742856194, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848021569126401, 2070846233742856194, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848021720121345, 2070846233742856194, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:33:41', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848400952311810, 2070848326482444289, 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 1, 0, 0, 20, '黑体', '标准值', 0, 2, '检测到段前段后间距检查不符合要求，当前值：黑体，期望值：标准值', '2026-06-27 12:35:12', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848401120083969, 2070848326482444289, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 2, 1, 10, 30, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:35:12', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848401287856130, 2070848326482444289, 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 3, 2, 20, 40, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:35:12', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848401447239681, 2070848326482444289, 'SPACE_CHECK', '空格检查', 'layout', 4, 3, 30, 50, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:35:12', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848401602428930, 2070848326482444289, 'FONT_SCALING', '字体缩放检查', 'font', 5, 4, 40, 60, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:35:12', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848401753423873, 2070848326482444289, 'TABLE_FONT', '表格内字体检查', 'table', 1, 5, 50, 70, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:35:12', NULL, 0);
INSERT INTO `t_dark_detect_result` (`id`, `task_id`, `rule_code`, `rule_name`, `rule_category`, `page_no`, `paragraph_index`, `start_offset`, `end_offset`, `actual_value`, `expected_value`, `is_pass`, `severity`, `remark`, `create_time`, `update_time`, `del_flag`) VALUES (2070848401921196033, 2070848326482444289, 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 2, 6, 60, 80, '标准值', '标准值', 1, 1, '检测通过', '2026-06-27 12:35:12', NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_detect_task
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_detect_task`;
CREATE TABLE `t_dark_detect_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `batch_id` bigint(20) NOT NULL COMMENT '所属批次ID',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号（唯一）',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` tinyint(4) DEFAULT NULL COMMENT '1-DOCX，2-PDF',
  `file_order` int(11) DEFAULT '0' COMMENT '文件在批次中的序号',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-排队中，1-检测中，2-已完成，3-失败',
  `total_rules` int(11) DEFAULT '0' COMMENT '总检测规则数',
  `current_rule_index` int(11) DEFAULT NULL COMMENT '当前检测规则索引（从0开始）',
  `pass_rules` int(11) DEFAULT '0' COMMENT '通过规则数',
  `fail_rules` int(11) DEFAULT '0' COMMENT '不通过规则数',
  `result_summary` text COMMENT '检测结果摘要（JSON）',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败时的错误信息',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_file_name` (`file_name`(100))
) ENGINE=InnoDB AUTO_INCREMENT=2070848326482444290 DEFAULT CHARSET=utf8mb4 COMMENT='暗标检测任务表';

-- ----------------------------
-- Records of t_dark_detect_task
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070491446895681538, 2070475175483371521, 'TASK17824786034540', '2026级研究生调档函.pdf', '/tmp/dark_upload/2070475175483371521/4a796650-d299-4741-ba4a-0981b0eda1b4.pdf', 213630, 2, 1, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：2cm，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 20:56:43', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070491447067648002, 2070475175483371521, 'TASK17824786035071', '失物招领模板框架.docx', '/tmp/dark_upload/2070475175483371521/48be2b3b-b788-42d7-86fa-1756eb67603e.docx', 843041, 1, 2, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：左对齐，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 20:56:44', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070491515782930433, 2070475175483371521, 'TASK17824786198900', '2026级研究生调档函.pdf', '/tmp/dark_upload/2070475175483371521/a5f3bdcc-1e9b-4f39-ab62-df9e74dc8c77.pdf', 213630, 2, 1, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：14号，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 20:57:00', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070491515925536769, 2070475175483371521, 'TASK17824786199241', '失物招领模板框架.docx', '/tmp/dark_upload/2070475175483371521/21fc4f53-12fa-4281-ac41-e83ba8950438.docx', 843041, 1, 2, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：加粗，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 20:57:00', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070495799031328770, 2070475175483371521, 'TASK17824796410900', '2026级研究生调档函.pdf', '/tmp/dark_upload/2070475175483371521/5c8afcf3-625e-4336-ac4d-096732418105.pdf', 213630, 2, 1, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：加粗，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 21:14:01', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070495799211683842, 2070475175483371521, 'TASK17824796411401', '26132000001815332071_深圳哈米波特科技有限公司_20260611085932.pdf', '/tmp/dark_upload/2070475175483371521/e5ac7333-aeb0-4033-aca8-f12d2ea2388f.pdf', 152321, 2, 2, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：14号，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 21:14:01', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070495799769526273, 2070475175483371521, 'TASK17824796412732', '教职工学习平台新版本功能点.docx', '/tmp/dark_upload/2070475175483371521/288860c0-449d-4415-ad61-12af21125744.docx', 8838, 1, 3, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：宋体，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 21:14:01', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070495799966658562, 2070475175483371521, 'TASK17824796413203', '失物招领模板框架.docx', '/tmp/dark_upload/2070475175483371521/36fa59db-23f2-4460-89ef-8ac3d1477f44.docx', 843041, 1, 4, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：宋体，期望值：标准值\"]}', NULL, 'admin', '管理员', '2026-06-26 21:14:01', '2026-06-27 12:25:44', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070846233482809346, 2070846121297760257, 'TASK17825631911750', '教职工学习平台新版本功能点.docx', '/tmp/dark_upload/2070846121297760257/fc118dec-af2c-48dc-b117-fb17b8ed03a7.docx', 8838, 1, 1, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：单倍行距，期望值：标准值\"]}', NULL, 'admin', 'admin', '2026-06-27 20:26:31', '2026-06-27 12:34:24', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070846233608638465, 2070846121297760257, 'TASK17825631912111', '失物招领模板框架.docx', '/tmp/dark_upload/2070846121297760257/df434dc5-aa0a-4ea0-9925-8e2c8804fb28.docx', 843041, 1, 2, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：左对齐，期望值：标准值\"]}', NULL, 'admin', 'admin', '2026-06-27 20:26:31', '2026-06-27 12:34:24', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070846233742856194, 2070846121297760257, 'TASK17825631912422', '郑云天+Java开发+石家庄.pdf', '/tmp/dark_upload/2070846121297760257/94a3ef1f-eb9e-45aa-b318-a87cf36c9907.pdf', 234184, 2, 3, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：12号，期望值：标准值\"]}', NULL, 'admin', 'admin', '2026-06-27 20:26:31', '2026-06-27 12:34:24', 1);
INSERT INTO `t_dark_detect_task` (`id`, `batch_id`, `task_no`, `file_name`, `file_path`, `file_size`, `file_type`, `file_order`, `status`, `total_rules`, `current_rule_index`, `pass_rules`, `fail_rules`, `result_summary`, `error_msg`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070848326482444289, 2070848272929570818, 'TASK17825636901840', '失物招领模板框架.docx', '/tmp/dark_upload/2070848272929570818/f9b974ad-2860-4b73-821c-aa5037bbc2d7.docx', 843041, 1, 1, 2, 7, 7, 6, 1, '{\"violations\":[\"段前段后间距检查：检测到段前段后间距检查不符合要求，当前值：黑体，期望值：标准值\"]}', NULL, 'admin', 'admin', '2026-06-27 20:34:50', '2026-06-27 20:35:08', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_rule_config
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_rule_config`;
CREATE TABLE `t_dark_rule_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码（固定值）',
  `rule_group` varchar(50) DEFAULT NULL COMMENT '规则分组标识（同一规则下的多个参数共享同一分组）',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_category` varchar(50) NOT NULL COMMENT '分类：layout/font/table/sensitive/cross',
  `param_key` varchar(50) NOT NULL COMMENT '参数键',
  `param_name` varchar(100) DEFAULT NULL COMMENT '参数显示名称',
  `param_value` varchar(200) NOT NULL COMMENT '参数值',
  `param_unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `param_type` varchar(30) DEFAULT 'TEXT' COMMENT '参数类型：TEXT/NUMBER/FLOAT/SELECT/MULTI_SELECT/BOOLEAN/FIXED',
  `param_options` text COMMENT '可选选项列表（JSON数组）',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-禁用，1-启用',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_rule_code` (`rule_code`),
  KEY `idx_category` (`rule_category`),
  KEY `idx_rule_group` (`rule_group`),
  KEY `idx_param_type` (`param_type`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COMMENT='检测规则配置表';

-- ----------------------------
-- Records of t_dark_rule_config
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (28, 'PAPER_SIZE', 'PAPER_SETTING', '纸张大小检查', 'layout', 'paper_type', '纸张类型', 'A4', '', 'FIXED', '[\"A4\"]', 1, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (29, 'PAPER_ORIENTATION', 'PAPER_SETTING', '纸张方向', 'layout', 'orientation', '纸张方向', 'portrait', '', 'FIXED', '[\"纵向\"]', 2, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (30, 'PAGE_MARGIN', 'PAGE_MARGIN', '页边距检查', 'layout', 'margin_top', '上边距', '2.5', 'cm', 'FLOAT', NULL, 3, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (31, 'PAGE_MARGIN', 'PAGE_MARGIN', '页边距检查', 'layout', 'margin_bottom', '下边距', '2.5', 'cm', 'FLOAT', NULL, 4, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (32, 'PAGE_MARGIN', 'PAGE_MARGIN', '页边距检查', 'layout', 'margin_left', '左边距', '2.5', 'cm', 'FLOAT', NULL, 5, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (33, 'PAGE_MARGIN', 'PAGE_MARGIN', '页边距检查', 'layout', 'margin_right', '右边距', '2.5', 'cm', 'FLOAT', NULL, 6, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (34, 'HEADER_FOOTER', 'HEADER_FOOTER', '页眉页脚检查', 'layout', 'check_header', '检查页眉', 'true', '', 'BOOLEAN', NULL, 7, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (35, 'HEADER_FOOTER', 'HEADER_FOOTER', '页眉页脚检查', 'layout', 'check_footer', '检查页脚', 'true', '', 'BOOLEAN', NULL, 8, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (36, 'CONTENT_CHECK', 'CONTENT_CHECK', '目录检查', 'layout', 'check_table_of_contents', '检查目录', 'true', '', 'BOOLEAN', NULL, 9, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (37, 'PARAGRAPH_ALIGN', 'PARAGRAPH_ALIGN', '段落对齐检查', 'layout', 'alignment', '对齐方式', 'left', '', 'SELECT', '[\"left:左对齐\",\"right:右对齐\",\"center:居中对齐\",\"justify:两端对齐\"]', 10, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (38, 'LINE_SPACING', 'LINE_SPACING', '行间距检查', 'layout', 'line_spacing', '行间距', '28', 'pt', 'NUMBER', NULL, 11, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (39, 'FIRST_LINE_INDENT', 'FIRST_LINE_INDENT', '首行缩进检查', 'layout', 'indent_chars', '缩进字符数', '2', '字符', 'NUMBER', NULL, 12, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (40, 'PARAGRAPH_SPACE', 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 'space_before', '段前间距', '0', 'pt', 'NUMBER', NULL, 13, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (41, 'PARAGRAPH_SPACE', 'PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 'space_after', '段后间距', '0', 'pt', 'NUMBER', NULL, 14, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (42, 'SPECIAL_FORMAT', 'SPECIAL_FORMAT', '特殊格式检查', 'layout', 'forbidden_styles', '禁止格式', 'bold,underline,italic', '', 'MULTI_SELECT', '[\"bold:禁止加粗\",\"underline:禁止下划线\",\"italic:禁止斜体\"]', 15, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (43, 'BACKGROUND_SHADING', 'BACKGROUND_SHADING', '底纹检查', 'layout', 'no_shading', '无底纹', 'true', '', 'BOOLEAN', NULL, 16, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (44, 'PARAGRAPH_PROPERTY', 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 'indent_before', '文本之前缩进', '2', '字符', 'NUMBER', NULL, 17, 1, 'admin', '管理员', '2026-06-26 04:10:01', '2026-06-26 18:51:51', 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (45, 'PARAGRAPH_PROPERTY', 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 'indent_after', '文本之后缩进', '0', '字符', 'NUMBER', NULL, 18, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (46, 'PARAGRAPH_PROPERTY', 'PARAGRAPH_PROPERTY', '段落属性', 'layout', 'direction', '输入方向', 'left_to_right', '', 'FIXED', '[\"left_to_right:从左到右\"]', 19, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (47, 'SPACE_CHECK', 'SPACE_CHECK', '空格检查', 'layout', 'check_spaces', '检查空格', 'true', '', 'BOOLEAN', NULL, 20, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (48, 'TEXT_COLOR', 'TEXT_COLOR', '文字颜色检查', 'font', 'font_color', '文字颜色', 'black', '', 'SELECT', '[\"black:黑色\"]', 21, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (49, 'ENGLISH_PUNCTUATION', 'ENGLISH_PUNCTUATION', '英文标点符号检查', 'font', 'use_chinese_punctuation', '使用中文标点', 'true', '', 'BOOLEAN', NULL, 22, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (50, 'FONT_SCALING', 'FONT_SCALING', '字体缩放检查', 'font', 'allow_scaling', '允许字体缩放', 'false', '', 'BOOLEAN', NULL, 23, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (51, 'BODY_FONT', 'BODY_FONT', '正文字体检查', 'font', 'font_name', '字体名称', '宋体', '', 'SELECT', '[\"宋体\",\"仿宋\",\"黑体\",\"楷体\",\"Times New Roman\",\"Arial\"]', 24, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (52, 'BODY_FONT_SIZE', 'BODY_FONT_SIZE', '正文字号检查', 'font', 'font_size', '字号', '14', 'pt', 'SELECT', '[\"10.5:10.5号（五号）\",\"12:12号（小四号）\",\"14:14号（四号）\",\"15:15号（小三号）\",\"16:16号（三号）\"]', 25, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (53, 'CHARACTER_SPACING', 'CHARACTER_SPACING', '字符间距检查', 'font', 'char_spacing', '字符间距', '0', 'pt', 'FLOAT', NULL, 26, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (54, 'TABLE_FONT', 'TABLE_FONT', '表格内字体检查', 'table', 'table_font_name', '表格字体', '宋体', '', 'SELECT', '[\"宋体\",\"仿宋\",\"黑体\",\"楷体\",\"Times New Roman\",\"Arial\"]', 30, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (55, 'TABLE_FONT_SIZE', 'TABLE_FONT_SIZE', '表格内字号检查', 'table', 'table_font_size', '表格字号', '14', 'pt', 'SELECT', '[\"10.5:10.5号（五号）\",\"12:12号（小四号）\",\"14:14号（四号）\",\"15:15号（小三号）\",\"16:16号（三号）\"]', 31, 1, 'admin', NULL, '2026-06-26 04:10:01', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (56, 'TABLE_TEXT_COLOR', 'TABLE_TEXT_COLOR', '表格内文字颜色检查', 'table', 'table_font_color', '表格文字颜色', 'black', '', 'FIXED', '[\"black:黑色\"]', 32, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (57, 'TABLE_TEXT_STYLE', 'TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'no_bold', '不加粗', 'true', '', 'BOOLEAN', NULL, 33, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (58, 'TABLE_TEXT_STYLE', 'TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'no_italic', '不倾斜', 'true', '', 'BOOLEAN', NULL, 34, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (59, 'TABLE_TEXT_STYLE', 'TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'no_underline', '不带下划线', 'true', '', 'BOOLEAN', NULL, 35, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (60, 'TABLE_TEXT_STYLE', 'TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'no_strikethrough', '不带着重号', 'true', '', 'BOOLEAN', NULL, 36, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (61, 'CHART_CHECK', 'CHART_CHECK', '图表检查', 'chart', 'check_charts', '检查图表', 'true', '', 'BOOLEAN', NULL, 40, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (62, 'TITLE_FONT', 'TITLE_FONT', '正文标题字体检查', 'title', 'title_font_name', '标题字体', '黑体', '', 'SELECT', '[\"宋体\",\"仿宋\",\"黑体\",\"楷体\",\"Times New Roman\",\"Arial\"]', 41, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (63, 'TITLE_FONT_SIZE', 'TITLE_FONT_SIZE', '正文标题字号检查', 'title', 'title_font_size', '标题字号', '16', 'pt', 'SELECT', '[\"10.5:10.5号（五号）\",\"12:12号（小四号）\",\"14:14号（四号）\",\"15:15号（小三号）\",\"16:16号（三号）\"]', 42, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (64, 'TITLE_STYLE', 'TITLE_STYLE', '正文标题样式检查', 'title', 'title_bold', '标题加粗', 'true', '', 'BOOLEAN', NULL, 43, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (65, 'TITLE_STYLE', 'TITLE_STYLE', '正文标题样式检查', 'title', 'title_color', '标题颜色', 'black', '', 'FIXED', '[\"black:黑色\"]', 44, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (66, 'TITLE_STYLE', 'TITLE_STYLE', '正文标题样式检查', 'title', 'title_no_italic', '不倾斜', 'true', '', 'BOOLEAN', NULL, 45, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (67, 'TITLE_STYLE', 'TITLE_STYLE', '正文标题样式检查', 'title', 'title_no_underline', '不带下划线', 'true', '', 'BOOLEAN', NULL, 46, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (68, 'TITLE_STYLE', 'TITLE_STYLE', '正文标题样式检查', 'title', 'title_no_strikethrough', '不带着重号', 'true', '', 'BOOLEAN', NULL, 47, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (69, 'TITLE_STYLE', 'TITLE_STYLE', '正文标题样式检查', 'title', 'title_style', '标题样式', 'luoyang', '', 'FIXED', '[\"luoyang:洛阳\"]', 48, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (70, 'TOC_SETTING', 'TOC_SETTING', '目录设置', 'toc', 'hide_page_numbers', '不显示页码', 'false', '', 'BOOLEAN', NULL, 50, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (71, 'TOC_SETTING', 'TOC_SETTING', '目录设置', 'toc', 'no_strikethrough', '不带着重号', 'true', '', 'BOOLEAN', NULL, 51, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (72, 'SELF_CHECK', 'SELF_CHECK', '自检项目', 'self_check', 'enable_self_check', '启用自检', 'true', '', 'BOOLEAN', NULL, 52, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (73, 'PAGE_COUNT_CHECK', 'PAGE_COUNT_CHECK', '文档页数检查', 'page_count', 'max_pages', '最大页数', '50', '页', 'NUMBER', NULL, 53, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (74, 'SENSITIVE_CHECK', 'SENSITIVE_CHECK', '敏感词检测', 'sensitive', 'enable_sensitive', '启用敏感词检测', 'true', '', 'BOOLEAN', NULL, 60, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (75, 'SENSITIVE_CROSS', 'SENSITIVE_CROSS', '敏感词跨文件检测', 'cross', 'check_sensitive_cross', '检测敏感词一致性', 'true', '', 'BOOLEAN', NULL, 70, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (76, 'FORMAT_CONSISTENCY', 'FORMAT_CONSISTENCY', '格式一致性检查', 'cross', 'check_font_consistency', '检测字体一致性', 'true', '', 'BOOLEAN', NULL, 71, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
INSERT INTO `t_dark_rule_config` (`id`, `rule_code`, `rule_group`, `rule_name`, `rule_category`, `param_key`, `param_name`, `param_value`, `param_unit`, `param_type`, `param_options`, `sort_order`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (77, 'FORMAT_CONSISTENCY', 'FORMAT_CONSISTENCY', '格式一致性检查', 'cross', 'check_page_margin_consistency', '检测页边距一致性', 'true', '', 'BOOLEAN', NULL, 72, 1, 'admin', NULL, '2026-06-26 04:10:02', NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_rule_scheme
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_rule_scheme`;
CREATE TABLE `t_dark_rule_scheme` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `scheme_name` varchar(100) NOT NULL COMMENT '方案名称',
  `scheme_desc` varchar(500) DEFAULT NULL COMMENT '方案描述',
  `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否默认方案：0-否，1-是',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-禁用，1-启用',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_creator_name` (`creator_name`),
  KEY `idx_is_default` (`is_default`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=2070459497120436227 DEFAULT CHARSET=utf8mb4 COMMENT='规则方案表';

-- ----------------------------
-- Records of t_dark_rule_scheme
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_rule_scheme` (`id`, `scheme_name`, `scheme_desc`, `is_default`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070446135129063425, '河北方案', '河北方案', 1, 1, '管理员', '管理员', '2026-06-26 17:56:40', '2026-06-27 21:01:44', 0);
INSERT INTO `t_dark_rule_scheme` (`id`, `scheme_name`, `scheme_desc`, `is_default`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070459497120436226, '河南方案', '河南方案', 0, 1, '管理员', '管理员', '2026-06-26 18:49:46', '2026-06-27 21:01:58', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_rule_scheme_item
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_rule_scheme_item`;
CREATE TABLE `t_dark_rule_scheme_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置项ID',
  `scheme_id` bigint(20) NOT NULL COMMENT '所属方案ID',
  `rule_config_id` bigint(20) NOT NULL COMMENT '关联规则配置ID',
  `param_value` varchar(200) DEFAULT NULL COMMENT '用户设定的参数值',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用该规则：0-禁用，1-启用',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_scheme_id` (`scheme_id`),
  KEY `idx_rule_config_id` (`rule_config_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=2070855152296984578 DEFAULT CHARSET=utf8mb4 COMMENT='方案规则配置表';

-- ----------------------------
-- Records of t_dark_rule_scheme_item
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070447303519604738, 2070446135129063425, 44, '2', 1, '管理员', '管理员', '2026-06-26 18:01:19', '2026-06-26 18:01:19', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070447303687376898, 2070446135129063425, 45, '2', 1, '管理员', '管理员', '2026-06-26 18:01:19', '2026-06-26 18:01:19', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070447495832637442, 2070446135129063425, 44, '2', 1, '管理员', '管理员', '2026-06-26 18:02:05', '2026-06-26 18:02:05', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070447495975243777, 2070446135129063425, 45, '2', 1, '管理员', '管理员', '2026-06-26 18:02:05', '2026-06-26 18:02:05', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070447496117850113, 2070446135129063425, 41, '0', 1, '管理员', '管理员', '2026-06-26 18:02:05', '2026-06-26 18:02:05', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070451929937580034, 2070446135129063425, 41, '0', 1, '管理员', '管理员', '2026-06-26 18:19:42', '2026-06-26 18:19:42', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070451930105352194, 2070446135129063425, 44, '2', 1, '管理员', '管理员', '2026-06-26 18:19:42', '2026-06-26 18:19:42', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070451930264735746, 2070446135129063425, 45, '2', 1, '管理员', '管理员', '2026-06-26 18:19:42', '2026-06-26 18:19:42', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070451930419924994, 2070446135129063425, 47, 'true', 1, '管理员', '管理员', '2026-06-26 18:19:42', '2026-06-26 18:19:42', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070451930575114241, 2070446135129063425, 50, 'true', 1, '管理员', '管理员', '2026-06-26 18:19:42', '2026-06-26 18:19:42', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452017195880450, 2070446135129063425, 41, '0', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452017376235521, 2070446135129063425, 44, '2', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452017564979202, 2070446135129063425, 45, '2', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452017745334273, 2070446135129063425, 47, 'true', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452017925689346, 2070446135129063425, 50, 'true', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452018110238722, 2070446135129063425, 54, '仿宋', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070452018290593793, 2070446135129063425, 55, '12', 1, '管理员', '管理员', '2026-06-26 18:20:03', '2026-06-26 18:20:03', 1);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855098572144641, 2070446135129063425, 41, '0', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855098735722498, 2070446135129063425, 44, '4', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855098886717442, 2070446135129063425, 45, '2', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855099029323778, 2070446135129063425, 47, 'true', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855099176124417, 2070446135129063425, 50, 'true', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855099318730753, 2070446135129063425, 54, '仿宋', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855099457142786, 2070446135129063425, 55, '12', 1, '管理员', '管理员', '2026-06-27 21:01:45', '2026-06-27 21:01:45', 0);
INSERT INTO `t_dark_rule_scheme_item` (`id`, `scheme_id`, `rule_config_id`, `param_value`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070855152296984577, 2070459497120436226, 45, '2', 1, '管理员', '管理员', '2026-06-27 21:01:58', '2026-06-27 21:01:58', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_sensitive_category
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_sensitive_category`;
CREATE TABLE `t_dark_sensitive_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称（如：投标招标、联系方式）',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序（前端展示顺序）',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-禁用，1-启用',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='敏感词分类表';

-- ----------------------------
-- Records of t_dark_sensitive_category
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (1, '投标招标', 1, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2, '联系方式', 2, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (3, '地理位置', 3, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (4, '网络信息', 4, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (5, '版权信息', 5, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (6, '作者信息', 6, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (7, '落地案例', 7, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (8, '奖项荣誉', 8, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (9, '专利信息', 9, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_category` (`id`, `category_name`, `sort_order`, `enabled`, `remark`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (10, '其他', 10, 1, NULL, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for t_dark_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `t_dark_sensitive_word`;
CREATE TABLE `t_dark_sensitive_word` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `category_id` bigint(20) NOT NULL COMMENT '所属分类ID（关联t_dark_sensitive_category.id）',
  `word` varchar(100) NOT NULL COMMENT '敏感词内容',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-禁用，1-启用',
  `creator_name` varchar(50) NOT NULL COMMENT '创建人姓名',
  `updater_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_word` (`word`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=2070161808835137539 DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ----------------------------
-- Records of t_dark_sensitive_word
-- ----------------------------
BEGIN;
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (1, 1, '投标', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2, 1, '招标', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (3, 1, '标书', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (4, 1, '竞标', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (5, 2, '团队', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (6, 2, '联系', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (7, 2, '电话', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (8, 2, '邮箱', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (9, 2, '地址', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (10, 2, '手机', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (11, 2, '传真', 1, 'admin', '管理员', '2026-06-24 08:55:30', '2026-06-25 17:56:44', 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (12, 2, '热线', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (13, 2, '姓名', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (14, 2, '有限公司', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (15, 2, '有限责任公司', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (16, 2, '有限股份公司', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (17, 3, '省', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (18, 3, '市', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (19, 3, '县', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (20, 3, '区', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (21, 3, '街道', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (22, 3, '路', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (23, 3, '巷', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (24, 3, '村', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (25, 3, '镇', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (26, 3, '乡', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (27, 4, '网址', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (28, 4, '网站', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (29, 4, '链接', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (30, 4, 'URL', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (31, 4, '域名', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (32, 4, 'IP', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (33, 5, 'copyright', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (34, 5, '版权所有', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (35, 5, '©', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (36, 5, '®', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (37, 5, '™', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (38, 5, '专利', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (39, 5, '商标', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (40, 6, 'author', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (41, 6, '作者', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (42, 6, '编写', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (43, 6, '撰写', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (44, 7, '落地', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (45, 7, '案例', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (46, 7, '实例', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (47, 7, '实践', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (48, 7, '应用', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (49, 7, '实施', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (50, 8, '奖项', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (51, 8, '荣誉', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (52, 8, '获奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (53, 8, '金奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (54, 8, '银奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (55, 8, '铜奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (56, 8, '一等奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (57, 8, '二等奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (58, 8, '三等奖', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (59, 9, '专利', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (60, 9, '发明专利', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (61, 9, '实用新型', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (62, 9, '外观设计', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (63, 9, '专利号', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (64, 9, '专利申请', 1, 'admin', NULL, '2026-06-24 08:55:30', NULL, 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070048892265881602, 1, '招投标', 1, '管理员', '管理员', '2026-06-25 15:38:10', '2026-06-25 15:38:10', 0);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070101919156850689, 2, '111', 1, '管理员', '管理员', '2026-06-25 19:08:53', '2026-06-25 19:08:53', 1);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070102617294557185, 1, '111', 1, '管理员', '管理员', '2026-06-25 19:11:39', '2026-06-25 19:11:39', 1);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070102719891427330, 1, '222', 1, '管理员', '管理员', '2026-06-25 19:12:04', '2026-06-25 19:12:04', 1);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070161518769655810, 1, '111', 1, '管理员', '管理员', '2026-06-25 23:05:42', '2026-06-25 23:05:42', 1);
INSERT INTO `t_dark_sensitive_word` (`id`, `category_id`, `word`, `enabled`, `creator_name`, `updater_name`, `create_time`, `update_time`, `del_flag`) VALUES (2070161808835137538, 1, '222', 1, '管理员', '管理员', '2026-06-25 23:06:52', '2026-06-25 23:06:52', 1);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
