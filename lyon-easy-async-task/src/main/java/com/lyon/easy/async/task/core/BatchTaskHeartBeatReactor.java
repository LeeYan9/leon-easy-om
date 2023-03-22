package com.lyon.easy.async.task.core;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Lyon
 */
@RequiredArgsConstructor
public class BatchTaskHeartBeatReactor {

    private final BatchTaskManager taskManager;
    @SuppressWarnings("FieldCanBeLocal")
    private ScheduledThreadPoolExecutor threadPoolExecutor;

    void init() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().setDaemon(true).setNamePrefix("task-heart-beat-").build();
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        this.threadPoolExecutor.schedule(new HeartBeatRunnable(), taskManager.getExecutorConfig().getHearBeatTimeMills(), TimeUnit.MILLISECONDS);
    }

    class HeartBeatRunnable implements Runnable {

        @Override
        public void run() {
            final long timeoutCheckTimeMs = taskManager.getExecutorConfig().getHearBeatTimeMills() * taskManager.getExecutorConfig().getHearBeatTimeoutInterval();
            // per idc all right to release lock when heartbeat expired
            taskManager.releaseLockWhenHeartBeatExpireIn();
            taskManager.renewHeartbeat();
        }
    }

}
