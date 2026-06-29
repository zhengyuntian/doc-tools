# 前端编码规则

## 技术规范
- 使用 `<script lang="ts" setup>` 组合式 API
- 列表页统一使用 `useView` Hook 管理状态
- HTTP 请求使用 `baseService`（封装 get/post/put/delete）
- 路由使用 hash 模式，基础地址 `http://localhost:8001/#/`
- 动态路由由后端菜单接口返回 `path` 生成，前端不自定义路由规则

## 文件位置
- 视图文件必须放在 `renren-ui/src/views/` 下
- 业务模块视图放在 `src/views/demo/` 或 `src/views/sys/` 等子目录
- 后端菜单 `path` 直接映射前端视图文件名，如 `demo/darkdetectbatch` 对应 `demo/darkdetectbatch.vue`

## 代码风格
- 组件名大驼峰，文件命名使用 kebab-case
- 图标使用 `@element-plus/icons-vue` 2.3.1
- 状态管理使用 Pinia，缓存使用工具函数 `cache.ts`
- 样式使用 Less，主题变量在 `assets/theme/` 下
- 通用组件前缀 `ren-`，如 `ren-dept-tree`