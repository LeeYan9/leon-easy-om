package com.lyon.easy.test.task.config;

import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Lyon
 */
@Configuration
public class AsyncTaskExecutorConfig {


    @Bean
    public ExecutorConfig executorConfig(List<TaskGroupConfig> taskGroupConfigs) {
        final ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setTaskGroupConfigs(taskGroupConfigs);
        executorConfig.setTablePrefix("lyon_");
        return executorConfig;
    }

    @Bean
    public TaskGroupConfig mallTaskGroupConfig() {
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        taskGroupConfig.setGroups(List.of("order-stat","product-stat"));
        taskGroupConfig.setExecutorId("mall-executor");
        taskGroupConfig.setExecCoreSize(5);
        taskGroupConfig.setExecMaxSize(20);
        taskGroupConfig.setName("mall-executor");
        return taskGroupConfig;
    }

    @Bean
    public TaskGroupConfig defaultTaskGroupConfig() {
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        taskGroupConfig.setGroups(List.of("default"));
        taskGroupConfig.setExecutorId("default-executor");
        taskGroupConfig.setExecCoreSize(20);
        taskGroupConfig.setExecMaxSize(20);
        taskGroupConfig.setName("default-executor");
        return taskGroupConfig;
    }

}
