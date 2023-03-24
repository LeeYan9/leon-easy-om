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
public class BatchTaskHeartBeatReactor {

    private final BatchTaskManager taskManager;
    @SuppressWarnings("FieldCanBeLocal")
    private ScheduledThreadPoolExecutor heartbeatScheduleExecutor;



    void init() {
        ThreadFactory threadFactory = ThreadFactoryBuilder.create().setDaemon(true).setNamePrefix("task-heartbeat-").build();
        this.heartbeatScheduleExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        this.heartbeatScheduleExecutor.schedule(new HeartBeatRunnable(), taskManager.getExecutorConfig().getHearBeatTimeMills(), TimeUnit.MILLISECONDS);
    }

    class HeartBeatRunnable implements Runnable {

        @Override
        public void run() {
            // per idc all right to release lock when heartbeat expired
            try {
                int affectedRows = taskManager.releaseLockWhenHeartBeatExpireIn();
                log.info("[heartbeat-srv] [heart-beat] release lock when heartbeat timeout  affectedRows [{}] ..", affectedRows);
                affectedRows = taskManager.renewHeartbeat();
                log.info("[heartbeat-srv] renew successful affectedRows [{}] ..", affectedRows);
            } catch (Exception e) {
                log.error("[heartbeat-srv] error", e);
            }
        }
    }

}
