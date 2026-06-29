package io.renren.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

public class DarkSoftDeleteInterceptor implements InnerInterceptor {

    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String T_DARK_PREFIX = "t_dark_";

    /**
     * 异步任务表列表（不需要软删除的表）
     */
    private static final String[] ASYNC_TABLES = {
        "t_dark_detect_cross_result"  // 关联分析结果表
    };

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        if (!isDarkTable(ms)) {
            return;
        }

        String sql = boundSql.getSql();
        if (!sql.toUpperCase().startsWith("SELECT")) {
            return;
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                if (plainSelect == null) {
                    return;
                }

                Table table = (Table) plainSelect.getFromItem();
                if (table == null) {
                    return;
                }

                String tableName = table.getName();
                if (!isDarkTable(tableName) || isAsyncTable(tableName)) {
                    return;
                }

                String tableNameForColumn = table.getAlias() != null ? table.getAlias().getName() : table.getName();

                Expression where = plainSelect.getWhere();
                EqualsTo delFlagCondition = new EqualsTo();
                delFlagCondition.setLeftExpression(new Column(tableNameForColumn + "." + DEL_FLAG_COLUMN));
                delFlagCondition.setRightExpression(new LongValue(0));

                if (where == null) {
                    plainSelect.setWhere(delFlagCondition);
                } else {
                    plainSelect.setWhere(new AndExpression(where, delFlagCondition));
                }

                PluginUtils.mpBoundSql(boundSql).sql(select.toString());
            }
        } catch (JSQLParserException e) {
        }
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        if (ms.getSqlCommandType() != SqlCommandType.DELETE) {
            return;
        }

        BoundSql boundSql = ms.getBoundSql(parameter);
        String sql = boundSql.getSql();
        if (!sql.toUpperCase().startsWith("DELETE")) {
            return;
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Delete) {
                Delete delete = (Delete) statement;
                Table table = delete.getTable();
                if (table == null) {
                    return;
                }

                String tableName = table.getName();
                if (!isDarkTable(tableName) || isAsyncTable(tableName)) {
                    return;
                }

                String whereClause = delete.getWhere() != null ? delete.getWhere().toString() : "";
                
                StringBuilder updateSql = new StringBuilder();
                updateSql.append("UPDATE ").append(tableName);
                updateSql.append(" SET ").append(DEL_FLAG_COLUMN).append("=1");
                
                if (whereClause.isEmpty()) {
                    updateSql.append(" WHERE ").append(DEL_FLAG_COLUMN).append("=0");
                } else {
                    updateSql.append(" WHERE ").append(whereClause);
                    updateSql.append(" AND ").append(DEL_FLAG_COLUMN).append("=0");
                }

                PluginUtils.mpBoundSql(boundSql).sql(updateSql.toString());
            }
        } catch (JSQLParserException e) {
            // 解析失败，忽略
        }
    }

    private boolean isDarkTable(MappedStatement ms) {
        String id = ms.getId();
        return id.contains("demo");
    }

    private boolean isDarkTable(String tableName) {
        return tableName != null && tableName.toLowerCase().startsWith(T_DARK_PREFIX);
    }

    /**
     * 检查是否为异步任务表（不需要软删除）
     */
    private boolean isAsyncTable(String tableName) {
        if (tableName == null) {
            return false;
        }
        for (String asyncTable : ASYNC_TABLES) {
            if (asyncTable.equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }
}
