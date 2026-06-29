# 项目约束与踩坑记录

## 硬约束
- 前端视图必须放在 `renren-ui/src/views` 目录下，放外部会导致动态路由 404
- 后端菜单 `path` 直接映射前端视图文件名，不要自定义路由规则
- JDK 版本必须是 17，修改 pom 后需重新加载 Maven

## 图标兼容性（@element-plus/icons-vue 2.3.1）
- `CheckCircle` → `CircleCheck`
- `XCircle` → `CircleClose`
- `FileQuestion` → `HelpFilled`
- `File` → `Document`
- `Play` → `VideoPlay`
- 使用不存在的图标会导致页面白屏

## 后端常见坑
- `Boolean` 类型判空使用 `Boolean.TRUE.equals(obj)`，避免 NPE
- 枚举值修改后需同步 Controller/Service，如 `WAITING` 改为 `QUEUED`
- `@Data` 在某些类上可能与框架冲突，必要时手写 getter/setter

## 数据库
- 时间字段使用 `datetime`，默认 `CURRENT_TIMESTAMP`
- 软删除字段 `del_flag` 默认 0，不要物理删除
- 状态值在注释中必须写清楚每个数字含义