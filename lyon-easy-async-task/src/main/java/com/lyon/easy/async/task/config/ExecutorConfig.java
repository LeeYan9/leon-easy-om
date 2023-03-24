package com.lyon.easy.async.task.config;

import com.lyon.easy.common.utils.CollUtils;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 执行器配置
 *
 * @author Lyon
 */
@Data
public class ExecutorConfig {

    private List<@Valid TaskGroupConfig> taskGroupConfigs = new ArrayList<>();

    private long hearBeatTimeMills = 1000 * 60 * 5;

    private long hearBeatTimeoutInterval = 6;

    /**
     * 通用缺省值：最大故障次数，任务失效次数>={{maxFailureCount}} 时，不再继续处理当前任务
     */
    private int maxFailureCount = 3;

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
