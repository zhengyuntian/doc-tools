# 异步任务表设计规则

## 适用范围
此规则适用于由系统异步任务自动生成数据的表，这些表无人工操作，不需要审计字段。

## 核心原则

### 异步任务表 vs 业务表
| 类型 | 特点 | 审计字段 |
|------|------|---------|
| 业务表（`t_dark_` 前缀） | 用户手动操作，需要记录操作人 | 需要 `creator_name`, `updater_name`, `del_flag` 等 |
| 异步任务表 | 系统自动填充，无人工干预 | 只需要 `create_time`，不需要其他审计字段 |

### 当前异步任务表
- `t_dark_detect_cross_result` - 关联分析结果表（批次检测异步任务生成）

## 实体类规范

### 业务实体（继承 BaseEntity）
```java
@Data
@TableName("t_dark_xxx")
public class DarkXxxEntity extends BaseEntity {
    // 继承审计字段：creator_name, updater_name, create_time, update_time, del_flag
    private Long id;
    private String name;
    // ...
}
```

### 异步任务实体（不继承 BaseEntity）
```java
@Data
@TableName("t_dark_detect_cross_result")
public class DarkDetectCrossResultEntity {
    @TableId
    private Long id;
    // 业务字段...
    
    // 只保留 create_time，无其他审计字段
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
```

## 常见错误

### 错误：异步任务实体继承 BaseEntity
```java
// 错误示例
public class DarkDetectCrossResultEntity extends BaseEntity {
    // 会尝试插入 creator_name, updater_name, del_flag
    // 但数据库表中没有这些字段，导致 SQL 错误
}
```

### 正确：异步任务实体独立定义
```java
// 正确示例
public class DarkDetectCrossResultEntity {
    @TableId
    private Long id;
    // ... 业务字段
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    // 无 creator_name, updater_name, del_flag
}
```

## MyBatis-Plus 软删除拦截器配置

### DarkSoftDeleteInterceptor 排除配置
异步任务表需要在 `DarkSoftDeleteInterceptor` 中排除，避免自动添加 `del_flag` 条件：

```java
// 在 DarkSoftDeleteInterceptor.java 中配置
private static final String[] ASYNC_TABLES = {
    "t_dark_detect_cross_result"  // 关联分析结果表
};

private boolean isAsyncTable(String tableName) {
    for (String asyncTable : ASYNC_TABLES) {
        if (asyncTable.equalsIgnoreCase(tableName)) {
            return true;
        }
    }
    return false;
}
```

### DAO 层规范
异步任务表的 DAO 可以继承 `BaseDao`，但需要确保拦截器已排除该表：

```java
@Mapper
public interface DarkDetectCrossResultDao extends BaseDao<DarkDetectCrossResultEntity> {
    // 继承 BaseDao 是安全的，因为拦截器已排除此表
}
```