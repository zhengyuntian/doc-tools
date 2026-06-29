# 项目架构规则

## 技术栈
- 后端：Spring Boot 4.0.5 + JDK17 + MyBatis-Plus 3.5.16 + Shiro 2.1.0 + Quartz
- 前端：Vue3 3.5.18 + Vite 5.4.19 + TypeScript 5.7 + Element-Plus 2.10.5 + Pinia 2.3.1
- 数据库：MySQL 8（默认），支持 Oracle/DM8/PostgreSQL/SQLServer

## 模块结构
- `renren-common`：公共模块，工具类、注解、异常、校验
- `renren-dynamic-datasource`：动态数据源
- `renren-admin`：管理后台，包含 sys（系统）、demo（业务）、job（定时任务）、oss（文件存储）
- `renren-api`：API 接口
- `renren-generator`：代码生成器

## 包分层规范
- `controller`：REST 接口层，使用 `@RestController`
- `service` / `service.impl`：业务逻辑层，继承 `CrudService` / `CrudServiceImpl`
- `dao`：数据访问层，继承 `BaseDao`
- `entity`：数据库实体，使用 `@TableName`
- `dto`：数据传输对象，用于接口出入参
- `excel`：Excel 导入导出实体
- `enums`：枚举类