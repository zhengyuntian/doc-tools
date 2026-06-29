# 页面设计规则

## 列表页统一布局
1. 顶部：条件筛选表单（`el-form :inline="true"`）
2. 中上：操作按钮（新增、删除、导出）
3. 中部：数据表格（`el-table`）
4. 底部：分页组件（`el-pagination`）
5. 弹窗：新增/修改表单（`xxx-add-or-update.vue`）

## 筛选表单规范
- 支持名称、状态、时间范围等条件查询
- 查询触发：`@keyup.enter="state.getDataList()"`
- 时间范围使用 `el-date-picker` type="daterange"

## 表格规范
- 必须有选择列（`type="selection"`）
- 操作列固定右侧（`fixed="right"`）
- 列宽、对齐方式明确设置
- 加载状态绑定 `state.dataListLoading`

## 分页规范
- 默认每页 10 条，可选 `[10, 20, 50, 100]`
- 布局：`total, sizes, prev, pager, next, jumper`

## 弹窗规范
- 组件名：`xxx-add-or-update.vue`
- 暴露 `init(id?)` 方法，无 id 为新增
- 保存成功后触发 `@refreshDataList="state.getDataList"`