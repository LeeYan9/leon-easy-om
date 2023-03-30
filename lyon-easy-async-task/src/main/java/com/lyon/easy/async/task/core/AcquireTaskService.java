package com.lyon.easy.async.task.core;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.common.primitives.Ints;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;
import com.lyon.easy.async.task.core.strategy.TaskAcquireStrategy;
import com.lyon.easy.async.task.core.strategy.DefaultTaskAcquireStrategy;
import com.lyon.easy.async.task.dal.dataobject.task.SubTaskDO;
import com.lyon.easy.common.utils.CollUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Lyon
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes", "AlibabaAvoidManuallyCreateThread"})
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

    private Map<String, TaskAcquireStrategy> acquireTaskTimingStrategyMap = new HashMap<>();

    private DelayQueue<TaskAcquireDelayRunnable> delayQueue = new DelayQueue();

    private BatchTaskManager taskManager;

    private ThreadPoolExecutor acquireTaskExecutors;

    private volatile boolean running;

    public AcquireTaskService(BatchTaskManager taskManager) {
        this.taskManager = taskManager;
        this.executorConfig = taskManager.getExecutorConfig();
        // init acquire task srv
        final int taskGroupSize = executorConfig.getTaskGroupConfigs().size();
        final ThreadFactory threadFactory = buildThreadFactory("[acquire-task-srv]-", false);

    }

    public void atOnceAcquire(String group) {
        final TaskGroupConfig taskGroupConfig = executorConfig.getTaskGroupConfigForGroup(group);
        Assert.notNull(taskGroupConfig);
        // at once acquire
        acquireTaskExecutors
                .submit(new TaskAcquireDelayRunnable(taskGroupConfig, SystemClock.now(), true));
    }

    public void init() {
        // 后期支持 custom spi
        final DefaultTaskAcquireStrategy defaultAcquireStrategy = new DefaultTaskAcquireStrategy();
        acquireTaskTimingStrategyMap.put(defaultAcquireStrategy.type(), defaultAcquireStrategy);
        final int groupSize = executorConfig.getTaskGroupConfigs().size();
        ThreadFactory threadFactory = buildThreadFactory("acquire-task-srv", false);
        acquireTaskExecutors = new ThreadPoolExecutor(groupSize, groupSize * 2,
                1, TimeUnit.MINUTES, new SynchronousQueue<>(), threadFactory);

        executorConfig
                .getTaskGroupConfigs()
                .forEach(taskGroupConfig -> delayQueue.add(new TaskAcquireDelayRunnable(taskGroupConfig, SystemClock.now(), false)));

        this.running = true;
        ThreadFactory threadFactory0 = buildThreadFactory("delay-checker-", true);
        threadFactory0.newThread(new DelayCheckerRunnable()).start();
    }

    private ThreadFactory buildThreadFactory(String s, boolean daemon) {
        return ThreadFactoryBuilder
                .create()
                .setDaemon(daemon)
                .setNamePrefix(s)
                .setUncaughtExceptionHandler((thread, throwable) -> log.error("{} thread pool error", s, throwable))
                .build();
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
            long diff = this.nextTimeStamp - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@SuppressWarnings("NullableProblems") Delayed delayed) {
            TaskAcquireDelayRunnable delayRunnable = ((TaskAcquireDelayRunnable) delayed);
            return Ints.saturatedCast(this.nextTimeStamp - delayRunnable.nextTimeStamp);
        }

        @Override
        public void run() {
            String executorId = taskGroupConfig.getExecutorId();
            try {
                log.info("[{}] acquire task-list start", executorId);
                // do something
                List<SubTaskDO> taskDOList = taskManager.determineTasksOfExec(taskGroupConfig);
                if (taskDOList.isEmpty()) {
                    log.info("[{}] ignore task list , acquire task-list is empty", executorId);
                    return;
                }
                final List<String> jobNos = CollUtils.toList(taskDOList, SubTaskDO::getJobNo);
                log.info("[{}] acquire task-list jobNos [{}] ", executorId, jobNos);
                for (SubTaskDO task : taskDOList) {
                    log.info("[{}] ready submit task jobNo [{}] ", executorId, task.getJobNo());
                    taskManager.kernelExecTask(taskGroupConfig, task);
                }
            } catch (Exception e) {
                log.error("execute error", e);
            } finally {
                if (!once) {
                    // 准备下次打捞任务
                    final long nextTimestamp = getNextTimestamp(taskGroupConfig);
                    delayQueue.add(new TaskAcquireDelayRunnable(taskGroupConfig, nextTimestamp, false));
                    log.info("[{}] wait next acquire task-list nextTimeMs[{}] intervalMs[{}] "
                            , taskGroupConfig.getExecutorId(), nextTimestamp, nextTimestamp - SystemClock.now());
                }
            }
        }

        private long getNextTimestamp(TaskGroupConfig taskGroupConfig) {
            final String acquireStrategy = taskGroupConfig.getAcquireStrategy();
            final TaskAcquireStrategy strategy = acquireTaskTimingStrategyMap.get(acquireStrategy);
            return strategy.getNextTimestamp(taskGroupConfig.getIntervalTimeMills());
        }
    }

    class DelayCheckerRunnable implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    final TaskAcquireDelayRunnable taskAcquireDelayRunnable = delayQueue.take();
                    final TaskGroupConfig taskGroupConfig = taskAcquireDelayRunnable.taskGroupConfig;
                    log.info("push [acquire-task-srv] [{}]", taskGroupConfig.getExecutorId());
                    acquireTaskExecutors.execute(taskAcquireDelayRunnable);
                } catch (InterruptedException e) {
                    log.error("delay-checker interrupted error", e);
                } catch (Exception e) {
                    log.error("delay-checker error", e);
                }
            }
        }
    }
}
