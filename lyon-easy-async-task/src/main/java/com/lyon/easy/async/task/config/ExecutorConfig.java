package com.lyon.easy.async.task.config;

import com.lyon.easy.common.utils.CollUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Lyon
 */
@Data
public class ExecutorConfig {

    private List<TaskGroupConfig> taskGroupConfigs = new ArrayList<>();

    private long hearBeatTimeMills = 1000 * 60 * 5;

    private long hearBeatTimeoutInterval = 6;

    private String tablePrefix;

    public TaskGroupConfig getTaskGroupConfigForGroup(String group) {
        if (Objects.isNull(taskGroupConfigs) || taskGroupConfigs.size() == 0) {
            return null;
        }

        return CollUtils.filterFirst(taskGroupConfigs, config -> config.getGroups().contains(group));
    }

    public TaskGroupConfig getTaskGroupConfig(String name) {
        if (Objects.isNull(taskGroupConfigs) || taskGroupConfigs.size() == 0) {
            return null;
        }
        return CollUtils.filterFirst(taskGroupConfigs, config -> config.getName().equals(name));
    }

}
