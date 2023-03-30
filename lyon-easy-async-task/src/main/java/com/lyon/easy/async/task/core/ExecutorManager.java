package com.lyon.easy.async.task.core;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Lyon
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ExecutorManager {

    private final Map<String, ThreadPoolExecutor> executorMap = new ConcurrentHashMap<>();
    private final ExecutorConfig executorConfig;

    public ExecutorManager(ExecutorConfig executorConfig) {
        this.executorConfig = executorConfig;
        for (TaskGroupConfig taskGroupConfig : executorConfig.getTaskGroupConfigs()) {
            String key = taskGroupConfig.getName();
            final ThreadFactory threadFactory = ThreadFactoryBuilder
                    .create()
                    .setNamePrefix(String.format("[async-task-executor-%s]-", taskGroupConfig.getName())).build();
            final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(taskGroupConfig.getExecCoreSize(), taskGroupConfig.getExecMaxSize(),
                    1, TimeUnit.MINUTES, new SynchronousQueue<>(), threadFactory);
            threadPoolExecutor.allowCoreThreadTimeOut(true);
            executorMap.put(key,threadPoolExecutor);
        }
    }

    public ThreadPoolExecutor getExecutor(String name) {
        final ThreadPoolExecutor executor = executorMap.get(name);
        Assert.notNull(executor);
        return executor;
    }

}
