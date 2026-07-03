-- ========================================
-- 检测规则配置内置数据（v2）
-- 包含完整的参数类型和可选选项配置
-- ========================================

-- 清空现有数据
DELETE FROM t_dark_rule_config WHERE del_flag = 0;

-- ========================================
-- 一、版式检测规则（layout）
-- ========================================

-- 1. 纸张大小检查（固定值，不可修改）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAPER_SIZE', '纸张大小检查', 'layout', 'PAPER_SETTING', 'paper_type', '纸张类型', 'A4', '', 'FIXED', '["A4"]', 1, 1, 'admin', NOW(), 0);

-- 2. 纸张方向（固定值，纵向）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAPER_ORIENTATION', '纸张方向', 'layout', 'PAPER_SETTING', 'orientation', '纸张方向', 'portrait', '', 'FIXED', '["纵向"]', 2, 1, 'admin', NOW(), 0);

-- 3. 页边距检查（上边距2.5厘米，其余均为2厘米）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAGE_MARGIN', '页边距检查', 'layout', 'PAGE_MARGIN', 'margin_top', '上边距', '2.5', 'cm', 'FLOAT', NULL, 3, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAGE_MARGIN', '页边距检查', 'layout', 'PAGE_MARGIN', 'margin_bottom', '下边距', '2.0', 'cm', 'FLOAT', NULL, 4, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAGE_MARGIN', '页边距检查', 'layout', 'PAGE_MARGIN', 'margin_left', '左边距', '2.0', 'cm', 'FLOAT', NULL, 5, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAGE_MARGIN', '页边距检查', 'layout', 'PAGE_MARGIN', 'margin_right', '右边距', '2.0', 'cm', 'FLOAT', NULL, 6, 1, 'admin', NOW(), 0);

-- 4. 页眉页脚检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('HEADER_FOOTER', '页眉页脚检查', 'layout', 'HEADER_FOOTER', 'check_header', '检查页眉', 'true', '', 'BOOLEAN', NULL, 7, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('HEADER_FOOTER', '页眉页脚检查', 'layout', 'HEADER_FOOTER', 'check_footer', '检查页脚', 'true', '', 'BOOLEAN', NULL, 8, 1, 'admin', NOW(), 0);

-- 5. 目录检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('CONTENT_CHECK', '目录检查', 'layout', 'CONTENT_CHECK', 'check_table_of_contents', '检查目录', 'true', '', 'BOOLEAN', NULL, 9, 1, 'admin', NOW(), 0);

-- 6. 封面检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('COVER_CHECK', '封面检查', 'layout', 'COVER_CHECK', 'check_cover', '检查封面', 'true', '', 'BOOLEAN', NULL, 10, 1, 'admin', NOW(), 0);

-- 7. 页码检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAGE_NUMBER_CHECK', '页码检查', 'layout', 'PAGE_NUMBER_CHECK', 'check_page_number', '检查页码', 'true', '', 'BOOLEAN', NULL, 11, 1, 'admin', NOW(), 0);

-- 8. 段落对齐检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PARAGRAPH_ALIGN', '段落对齐检查', 'layout', 'PARAGRAPH_ALIGN', 'alignment', '对齐方式', 'left', '', 'SELECT', '["left:左对齐","right:右对齐","center:居中对齐","justify:两端对齐"]', 12, 1, 'admin', NOW(), 0);

-- 9. 行间距检查（固定值30磅）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('LINE_SPACING', '行间距检查', 'layout', 'LINE_SPACING', 'line_spacing', '行间距', '30', 'pt', 'NUMBER', NULL, 13, 1, 'admin', NOW(), 0);

-- 10. 首行缩进检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('FIRST_LINE_INDENT', '首行缩进检查', 'layout', 'FIRST_LINE_INDENT', 'indent_chars', '缩进字符数', '2', '字符', 'NUMBER', NULL, 14, 1, 'admin', NOW(), 0);

-- 11. 段前段后间距检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 'PARAGRAPH_SPACE', 'space_before', '段前间距', '0', 'pt', 'NUMBER', NULL, 15, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PARAGRAPH_SPACE', '段前段后间距检查', 'layout', 'PARAGRAPH_SPACE', 'space_after', '段后间距', '0', 'pt', 'NUMBER', NULL, 16, 1, 'admin', NOW(), 0);

-- 12. 特殊格式检查（多选）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('SPECIAL_FORMAT', '特殊格式检查', 'layout', 'SPECIAL_FORMAT', 'forbidden_styles', '禁止格式', 'bold,underline,italic', '', 'MULTI_SELECT', '["bold:禁止加粗","underline:禁止下划线","italic:禁止斜体"]', 17, 1, 'admin', NOW(), 0);

-- 13. 无底纹检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('BACKGROUND_SHADING', '底纹检查', 'layout', 'BACKGROUND_SHADING', 'no_shading', '无底纹', 'true', '', 'BOOLEAN', NULL, 18, 1, 'admin', NOW(), 0);

-- 14. 段落属性
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PARAGRAPH_PROPERTY', '段落属性', 'layout', 'PARAGRAPH_PROPERTY', 'indent_before', '文本之前缩进', '0', '字符', 'NUMBER', NULL, 19, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PARAGRAPH_PROPERTY', '段落属性', 'layout', 'PARAGRAPH_PROPERTY', 'indent_after', '文本之后缩进', '0', '字符', 'NUMBER', NULL, 20, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PARAGRAPH_PROPERTY', '段落属性', 'layout', 'PARAGRAPH_PROPERTY', 'direction', '输入方向', 'left_to_right', '', 'FIXED', '["left_to_right:从左到右"]', 21, 1, 'admin', NOW(), 0);

-- 15. 空格检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('SPACE_CHECK', '空格检查', 'layout', 'SPACE_CHECK', 'check_spaces', '检查空格', 'true', '', 'BOOLEAN', NULL, 22, 1, 'admin', NOW(), 0);

-- ========================================
-- 二、字体检测规则（font）
-- ========================================

-- 16. 文字颜色检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TEXT_COLOR', '文字颜色检查', 'font', 'TEXT_COLOR', 'font_color', '文字颜色', 'black', '', 'SELECT', '["black:黑色"]', 23, 1, 'admin', NOW(), 0);

-- 17. 是否使用英文标点符号
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('ENGLISH_PUNCTUATION', '英文标点符号检查', 'font', 'ENGLISH_PUNCTUATION', 'use_chinese_punctuation', '使用中文标点', 'true', '', 'BOOLEAN', NULL, 24, 1, 'admin', NOW(), 0);

-- 18. 是否允许字体缩放
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('FONT_SCALING', '字体缩放检查', 'font', 'FONT_SCALING', 'allow_scaling', '允许字体缩放', 'false', '', 'BOOLEAN', NULL, 25, 1, 'admin', NOW(), 0);

-- 19. 正文字体检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('BODY_FONT', '正文字体检查', 'font', 'BODY_FONT', 'font_name', '字体名称', '宋体', '', 'SELECT', '["宋体","仿宋","黑体","楷体","Times New Roman","Arial"]', 26, 1, 'admin', NOW(), 0);

-- 20. 正文字号检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('BODY_FONT_SIZE', '正文字号检查', 'font', 'BODY_FONT_SIZE', 'font_size', '字号', '14', 'pt', 'SELECT', '["10.5:10.5号（五号）","12:12号（小四号）","14:14号（四号）","15:15号（小三号）","16:16号（三号）"]', 27, 1, 'admin', NOW(), 0);

-- 21. 字符间距检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('CHARACTER_SPACING', '字符间距检查', 'font', 'CHARACTER_SPACING', 'char_spacing', '字符间距', '0', 'pt', 'FLOAT', NULL, 28, 1, 'admin', NOW(), 0);

-- ========================================
-- 三、表格检测规则（table）
-- ========================================

-- 22. 表格内字体检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_FONT', '表格内字体检查', 'table', 'TABLE_FONT', 'table_font_name', '表格字体', '宋体', '', 'SELECT', '["宋体","仿宋","黑体","楷体","Times New Roman","Arial"]', 30, 1, 'admin', NOW(), 0);

-- 23. 表格内字号检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_FONT_SIZE', '表格内字号检查', 'table', 'TABLE_FONT_SIZE', 'table_font_size', '表格字号', '14', 'pt', 'SELECT', '["10.5:10.5号（五号）","12:12号（小四号）","14:14号（四号）","15:15号（小三号）","16:16号（三号）"]', 31, 1, 'admin', NOW(), 0);

-- 24. 表格内文字颜色
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_TEXT_COLOR', '表格内文字颜色检查', 'table', 'TABLE_TEXT_COLOR', 'table_font_color', '表格文字颜色', 'black', '', 'FIXED', '["black:黑色"]', 32, 1, 'admin', NOW(), 0);

-- 25. 表格内文字样式检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'TABLE_TEXT_STYLE', 'no_bold', '不加粗', 'true', '', 'BOOLEAN', NULL, 33, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'TABLE_TEXT_STYLE', 'no_italic', '不倾斜', 'true', '', 'BOOLEAN', NULL, 34, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'TABLE_TEXT_STYLE', 'no_underline', '不带下划线', 'true', '', 'BOOLEAN', NULL, 35, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_TEXT_STYLE', '表格内文字样式检查', 'table', 'TABLE_TEXT_STYLE', 'no_strikethrough', '不带着重号', 'true', '', 'BOOLEAN', NULL, 36, 1, 'admin', NOW(), 0);

-- 26. 禁止表格以图片格式插入
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_IMAGE_FORBIDDEN', '禁止表格以图片格式插入', 'table', 'TABLE_IMAGE_FORBIDDEN', 'forbid_image_table', '禁止图片表格', 'true', '', 'BOOLEAN', NULL, 37, 1, 'admin', NOW(), 0);

-- 27. 表内文字对齐方式检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_ALIGN', '表内文字对齐检查', 'table', 'TABLE_ALIGN', 'vertical_align', '垂直对齐', 'center', '', 'SELECT', '["top:顶端对齐","center:居中对齐","bottom:底端对齐"]', 38, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_ALIGN', '表内文字对齐检查', 'table', 'TABLE_ALIGN', 'horizontal_align', '水平对齐', 'left', '', 'SELECT', '["left:左对齐","center:居中对齐","right:右对齐"]', 39, 1, 'admin', NOW(), 0);

-- 28. 表内首行缩进检查（无首行缩进）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TABLE_INDENT', '表内首行缩进检查', 'table', 'TABLE_INDENT', 'no_first_line_indent', '无首行缩进', 'true', '', 'BOOLEAN', NULL, 40, 1, 'admin', NOW(), 0);

-- ========================================
-- 四、图表检测规则（chart）
-- ========================================

-- 28. 图表检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('CHART_CHECK', '图表检查', 'chart', 'CHART_CHECK', 'check_charts', '检查图表', 'true', '', 'BOOLEAN', NULL, 40, 1, 'admin', NOW(), 0);

-- 29. 图表颜色检查（黑色）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('CHART_COLOR', '图表颜色检查', 'chart', 'CHART_COLOR', 'chart_color', '图表颜色', 'black', '', 'FIXED', '["black:黑色"]', 41, 1, 'admin', NOW(), 0);

-- 16. 图、表整体对齐检查（居中对齐）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('FIGURE_ALIGN', '图表示例对齐检查', 'layout', 'FIGURE_ALIGN', 'alignment', '对齐方式', 'center', '', 'SELECT', '["left:左对齐","center:居中对齐","right:右对齐"]', 23, 1, 'admin', NOW(), 0);

-- ========================================
-- 五、正文标题检测规则（title）
-- ========================================

-- 25. 正文标题字体检查（宋体）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_FONT', '正文标题字体检查', 'title', 'TITLE_FONT', 'title_font_name', '标题字体', '宋体', '', 'SELECT', '["宋体","仿宋","黑体","楷体","Times New Roman","Arial"]', 41, 1, 'admin', NOW(), 0);

-- 26. 正文标题字号检查（四号14pt）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_FONT_SIZE', '正文标题字号检查', 'title', 'TITLE_FONT_SIZE', 'title_font_size', '标题字号', '14', 'pt', 'SELECT', '["10.5:10.5号（五号）","12:12号（小四号）","14:14号（四号）","15:15号（小三号）","16:16号（三号）"]', 42, 1, 'admin', NOW(), 0);

-- 27. 正文标题样式检查（常规，不加粗）
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_STYLE', '正文标题样式检查', 'title', 'TITLE_STYLE', 'title_bold', '标题加粗', 'false', '', 'BOOLEAN', NULL, 43, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_STYLE', '正文标题样式检查', 'title', 'TITLE_STYLE', 'title_color', '标题颜色', 'black', '', 'FIXED', '["black:黑色"]', 44, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_STYLE', '正文标题样式检查', 'title', 'TITLE_STYLE', 'title_no_italic', '不倾斜', 'true', '', 'BOOLEAN', NULL, 45, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_STYLE', '正文标题样式检查', 'title', 'TITLE_STYLE', 'title_no_underline', '不带下划线', 'true', '', 'BOOLEAN', NULL, 46, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_STYLE', '正文标题样式检查', 'title', 'TITLE_STYLE', 'title_no_strikethrough', '不带着重号', 'true', '', 'BOOLEAN', NULL, 47, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TITLE_STYLE', '正文标题样式检查', 'title', 'TITLE_STYLE', 'title_style', '标题样式', 'luoyang', '', 'FIXED', '["luoyang:洛阳"]', 48, 1, 'admin', NOW(), 0);

-- ========================================
-- 六、目录设置规则（toc）
-- ========================================

-- 28. 目录设置
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TOC_SETTING', '目录设置', 'toc', 'TOC_SETTING', 'hide_page_numbers', '不显示页码', 'false', '', 'BOOLEAN', NULL, 50, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('TOC_SETTING', '目录设置', 'toc', 'TOC_SETTING', 'no_strikethrough', '不带着重号', 'true', '', 'BOOLEAN', NULL, 51, 1, 'admin', NOW(), 0);

-- ========================================
-- 七、自检项目规则（self_check）
-- ========================================

-- 29. 自检项目
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('SELF_CHECK', '自检项目', 'self_check', 'SELF_CHECK', 'enable_self_check', '启用自检', 'true', '', 'BOOLEAN', NULL, 52, 1, 'admin', NOW(), 0);

-- ========================================
-- 八、文档页数检查规则（page_count）
-- ========================================

-- 30. 文档页数检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('PAGE_COUNT_CHECK', '文档页数检查', 'page_count', 'PAGE_COUNT_CHECK', 'max_pages', '最大页数', '50', '页', 'NUMBER', NULL, 53, 1, 'admin', NOW(), 0);

-- ========================================
-- 九、敏感词检测规则（sensitive）
-- ========================================

-- 31. 敏感词检测
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('SENSITIVE_CHECK', '敏感词检测', 'sensitive', 'SENSITIVE_CHECK', 'enable_sensitive', '启用敏感词检测', 'true', '', 'BOOLEAN', NULL, 60, 1, 'admin', NOW(), 0);

-- ========================================
-- 十、交叉检测规则（cross）
-- ========================================

-- 32. 敏感词跨文件一致性检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('SENSITIVE_CROSS', '敏感词跨文件检测', 'cross', 'SENSITIVE_CROSS', 'check_sensitive_cross', '检测敏感词一致性', 'true', '', 'BOOLEAN', NULL, 70, 1, 'admin', NOW(), 0);

-- 33. 格式一致性检查
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('FORMAT_CONSISTENCY', '格式一致性检查', 'cross', 'FORMAT_CONSISTENCY', 'check_font_consistency', '检测字体一致性', 'true', '', 'BOOLEAN', NULL, 71, 1, 'admin', NOW(), 0);
INSERT INTO t_dark_rule_config (rule_code, rule_name, rule_category, rule_group, param_key, param_name, param_value, param_unit, param_type, param_options, sort_order, enabled, creator_name, create_time, del_flag) VALUES
('FORMAT_CONSISTENCY', '格式一致性检查', 'cross', 'FORMAT_CONSISTENCY', 'check_page_margin_consistency', '检测页边距一致性', 'true', '', 'BOOLEAN', NULL, 72, 1, 'admin', NOW(), 0);

-- 查询验证
SELECT rule_category, rule_group, COUNT(*) as count FROM t_dark_rule_config WHERE del_flag = 0 GROUP BY rule_category, rule_group ORDER BY rule_category, rule_group;