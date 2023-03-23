package com.lyon.easy.async.task.config.mybatis;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lyon
 */
public class DefaultListableTableNameContainer {

    protected static final List<String> TABLE_NAME_LIST = new ArrayList<>();

    static {
        TABLE_NAME_LIST.add("sub_task");
        TABLE_NAME_LIST.add("batch_task");
    }

    public static List<String> getTableNames(){
        return TABLE_NAME_LIST;
    }

}
