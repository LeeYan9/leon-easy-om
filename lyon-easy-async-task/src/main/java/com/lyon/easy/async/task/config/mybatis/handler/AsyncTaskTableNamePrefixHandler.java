package com.lyon.easy.async.task.config.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import lombok.*;

/**
 * @author Lyon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskTableNamePrefixHandler implements TableNameHandler {

    private TableNamePrefix tableNamePrefix;

    @Override
    public String dynamicTableName(String sql, String tableName) {
        return tableNamePrefix.getPrefix() + tableName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AsyncTaskTableNamePrefixHandler target = new AsyncTaskTableNamePrefixHandler(new TableNamePrefix());

        public Builder prefix(String prefix) {
            target.getTableNamePrefix().setPrefix(prefix);
            return this;
        }

        public AsyncTaskTableNamePrefixHandler build() {
            return target;
        }


    }

    static class TableNamePrefix {
        @Getter
        @Setter
        private String prefix;
    }
}
