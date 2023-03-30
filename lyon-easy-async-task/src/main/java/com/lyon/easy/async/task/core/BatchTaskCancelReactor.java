package com.lyon.easy.async.task.core;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Lyon
 */
@RequiredArgsConstructor
@Slf4j
public class BatchTaskCancelReactor {

    private final BatchTaskManager taskManager;
    @SuppressWarnings("FieldCanBeLocal")
    private ScheduledThreadPoolExecutor taskCancelScheduleExecutor;

    void init() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().setDaemon(true).setNamePrefix("task-cancel-").build();
        this.taskCancelScheduleExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        this.taskCancelScheduleExecutor.schedule(new TaskCancelRunnable(), taskManager.getExecutorConfig().getTaskCancelTimeMills(), TimeUnit.MILLISECONDS);
    }

    class TaskCancelRunnable implements Runnable {

        @Override
        public void run() {
            try {
                log.info("[task-cancel-srv] exec start");
                // 直接取消批任务
                taskManager.doCancelTask();
                // 中断批任务
                taskManager.doInterruptTask();
                log.info("[task-cancel-srv] exec end");
            } catch (Exception e) {
                log.error("[task-cancel-srv] error", e);
            }
        }
    }

}
