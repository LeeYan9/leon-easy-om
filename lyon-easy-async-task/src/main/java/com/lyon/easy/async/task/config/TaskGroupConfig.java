package com.lyon.easy.async.task.config;

import lombok.Data;

import java.util.List;

/**
 * @author Lyon
 */
@Data
public class TaskGroupConfig {

    private String name;

    private String executorId;

    private List<String> groups;

    private int taskLimit;

    private int execCoreSize;

    private int execMaxSize;

    private int acquireCoreSize;

    private String acquireStrategy;

    private long intervalTimeMills;
}
