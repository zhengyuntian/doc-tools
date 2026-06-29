# 后端编码规则

## 命名规范
- 实体类：`XxxEntity`，DTO：`XxxDTO`，DAO：`XxxDao`，Service：`XxxService`
- Controller 映射路径使用小写无连字符，如 `demo/darkdetectbatch`
- 权限字符串格式：`模块:实体:操作`，如 `demo:darkdetectbatch:save`
- 数据库表名前缀：`t_dark_`（业务表）、`sys_`（系统表）

## 编码风格
- 使用 Lombok `@Data` 简化实体，但注意部分场景需手写 getter/setter
- 实体字段使用驼峰命名，与数据库下划线自动映射
- 时间类型使用 `java.util.Date`，状态使用 `Integer` / `tinyint`
- Service 实现类通过 `getWrapper(Map)` 构建查询条件，使用 Hutool `StrUtil` 判空
- 删除逻辑使用 `del_flag` 软删除，值为 0/1

## 接口规范
- 分页接口返回 `Result<PageData<DTO>>`
- 新增/修改/删除返回 `Result`
- 使用 `@LogOperation` 记录操作日志
- 使用 `@RequiresPermissions` 控制权限
- 入参校验使用 `ValidatorUtils.validateEntity(dto, Group.class)`

## 审计字段自动填充

### 业务表审计字段
业务表（`t_dark_` 前缀）自动处理以下审计字段：

| 字段名 | 数据库字段 | 填充时机 | 说明 |
|--------|-----------|---------|------|
| `creatorName` | `creator_name` | INSERT | 创建人姓名 |
| `updaterName` | `updater_name` | INSERT_UPDATE | 更新人姓名 |
| `createTime` | `create_time` | INSERT | 创建时间 |
| `updateTime` | `update_time` | INSERT_UPDATE | 更新时间 |
| `delFlag` | `del_flag` | INSERT | 软删除标记（0-未删除，1-已删除） |

### 实体类规范
- 业务实体必须继承 `BaseEntity` 抽象类
- `BaseEntity` 已包含审计字段和 `@TableField(fill = ...)` 注解
- 无需手动设置审计字段值

### 实现机制
1. **自动填充**：`FieldMetaObjectHandler` 在 INSERT/UPDATE 时自动填充字段值
2. **软删除**：`DarkSoftDeleteInterceptor` 将 DELETE 转换为 UPDATE `del_flag=1`
3. **查询过滤**：`DarkSoftDeleteInterceptor` 自动追加 `WHERE del_flag=0` 条件
4. **更新保护**：`DarkSoftDeleteInterceptor` 更新时自动追加 `AND del_flag=0`

### 使用示例
```java
// 业务实体只需继承 BaseEntity，无需手动处理审计字段
@Data
@TableName("t_dark_sensitive_word")
public class DarkSensitiveWordEntity extends BaseEntity {
    private Long id;
    private Long categoryId;
    private String word;
    private Integer enabled;
}
```