package com.lyon.easy.async.task.config.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import lombok.Data;

/**
 * @author Lyon
 */
@Data
public class AsyncTaskTableNamePrefixHandler implements TableNameHandler {

    private final TableNamePrefix tableNamePrefix;

    @Override
    public String dynamicTableName(String sql, String tableName) {
        return tableNamePrefix.getPrefix() + tableName;
    }

    @Data
    public static class TableNamePrefix {
        private final String prefix;
    }
}
