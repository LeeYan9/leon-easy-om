package com.lyon.easy.async.task.core;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;
import com.lyon.easy.async.task.core.strategy.AcquireStrategy;
import com.lyon.easy.async.task.core.strategy.DefaultAcquireStrategy;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Lyon
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class AcquireTaskService {

    // 单个线程根据 原始间隔时间、策略、去计算下次拉取时间拉取任务

    /**
     * 动态的情况：
     * 当前守护线程：每个组可以使用不同的间隔时间、策略，去计算下次拉取时间拉取任务
     */
    private ExecutorConfig executorConfig;

    private Map<String, AcquireStrategy> acquireTaskTimingStrategyMap = new HashMap<>();

    private DelayQueue delayQueue = new DelayQueue();

    private BatchTaskManager taskManager;

    private ThreadPoolExecutor acquireTaskExecutors;

    public AcquireTaskService(BatchTaskManager taskManager) {
        this.taskManager = taskManager;
        this.executorConfig = taskManager.getExecutorConfig();
        // init acquire task srv
        final int taskGroupSize = executorConfig.getTaskGroupConfigs().size();
        final ThreadFactory threadFactory = ThreadFactoryBuilder.create().setNamePrefix("acquire-task-srv-").build();
        this.acquireTaskExecutors = new ThreadPoolExecutor(taskGroupSize, taskGroupSize * 2,
                1, TimeUnit.MILLISECONDS, delayQueue, threadFactory);
        // 后期支持 custom spi
        final DefaultAcquireStrategy defaultAcquireStrategy = new DefaultAcquireStrategy();
        acquireTaskTimingStrategyMap.put(defaultAcquireStrategy.type(), defaultAcquireStrategy);
    }

    public void atOnceAcquire(String group) {
        final TaskGroupConfig taskGroupConfig = executorConfig.getTaskGroupConfigForGroup(group);
        Assert.notNull(taskGroupConfig);
        // at once acquire
        acquireTaskExecutors
                .submit(new TaskAcquireDelayRunnable(taskGroupConfig, SystemClock.now(), true));
    }

    public void init() {
        final int groupSize = executorConfig.getTaskGroupConfigs().size();
        final ThreadFactory threadFactory = ThreadFactoryBuilder.create().setNamePrefix("acquire-Task-").build();
        acquireTaskExecutors = new ThreadPoolExecutor(groupSize, groupSize * 2,
                1, TimeUnit.MINUTES, delayQueue, threadFactory);
        executorConfig
                .getTaskGroupConfigs()
                .forEach(taskGroupConfig -> acquireTaskExecutors.submit(new TaskAcquireDelayRunnable(taskGroupConfig)));
    }

    @AllArgsConstructor
    class TaskAcquireDelayRunnable implements Delayed, Runnable {
        public TaskAcquireDelayRunnable(TaskGroupConfig taskGroupConfig) {
            this.taskGroupConfig = taskGroupConfig;
            this.nextTimeStamp = SystemClock.now();
            this.once = false;
        }

        private final TaskGroupConfig taskGroupConfig;

        private final long nextTimeStamp;

        private final boolean once;

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.nextTimeStamp - SystemClock.now(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed delayed) {
            return (int) (delayed.getDelay(TimeUnit.MILLISECONDS) - this.nextTimeStamp);
        }

        @Override
        public void run() {
            try {
                // do something
                List<SubTaskDO> taskDOList = taskManager.determineTasksOfExec(taskGroupConfig);
                if (taskDOList.isEmpty()) {
                    log.info("acquire-task-service get sub-task-list is empty ");
                    return;
                }
                for (SubTaskDO task : taskDOList) {
                    taskManager.kernelExecTask(taskGroupConfig, task);
                }
            } catch (Exception e) {
                log.error("acquire-task-service exec error", e);
            } finally {
                if (!once) {
                    final long nextTimestamp = acquireNextTimestamp(taskGroupConfig);
                    acquireTaskExecutors.submit(new TaskAcquireDelayRunnable(taskGroupConfig, nextTimestamp, false));
                }
            }
        }

        private long acquireNextTimestamp(TaskGroupConfig taskGroupConfig) {
            final String acquireStrategy = taskGroupConfig.getAcquireStrategy();
            final AcquireStrategy strategy = acquireTaskTimingStrategyMap.get(acquireStrategy);
            return strategy.acquireNextTimestamp(taskGroupConfig.getIntervalTimeMills());
        }
    }

}
