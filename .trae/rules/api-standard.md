# 接口规范规则

## RESTful 风格
- 查询分页：`GET /{module}/{entity}/page`
- 单条详情：`GET /{module}/{entity}/{id}`
- 新增：`POST /{module}/{entity}`
- 修改：`PUT /{module}/{entity}`
- 删除：`DELETE /{module}/{entity}`（body 传 id 数组）
- 导出：`GET /{module}/{entity}/export`

## Swagger 文档
- Controller 使用 `@Tag(name = "中文名称")`
- 方法使用 `@Operation(summary = "简述")`
- 参数使用 `@Parameter` 标注分页字段（PAGE、LIMIT、ORDER_FIELD、ORDER）

## 统一响应
- 成功：`{ "code": 0, "msg": "success", "data": ... }`
- 失败：`code != 0`，前端通过 `ElMessage.error` 提示
- 401 未授权时前端自动跳转登录页

## 权限控制
- Controller 方法必须加 `@RequiresPermissions("模块:实体:操作")`
- 前端按钮使用 `v-if="state.hasPermission('...')"` 控制显隐
- 权限字符串与后端菜单 `permissions` 字段保持一致