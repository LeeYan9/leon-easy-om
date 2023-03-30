package com.lyon.easy.common.exec;

import cn.hutool.core.thread.NamedThreadFactory;
import com.lyon.easy.common.base.R;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lyon
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class DirectTimeoutExec {
    private final Logger logger = LoggerFactory.getLogger(DirectTimeoutExec.class);
    private final HashedWheelTimer timer;
    public static final Long MIN_TIMEOUT_MILLS = 100L;

    public static final int INIT = 0;
    public static final int CANCELLED = 0;
    public static final int FINISHED = 0;

    protected DirectTimeoutExec() {
        this(MIN_TIMEOUT_MILLS);
    }

    protected DirectTimeoutExec(Long tickDurationMills) {
        this.timer = new HashedWheelTimer(new NamedThreadFactory("DirectTimeoutExec-wheelTimer-", true), tickDurationMills, TimeUnit.MILLISECONDS);
    }

    protected DirectTimeoutExec(Long tickDurationMills, int ticksPerWheeL) {
        this.timer = new HashedWheelTimer(new NamedThreadFactory("DirectTimeoutExec-wheelTimer-", true), tickDurationMills, TimeUnit.MILLISECONDS, ticksPerWheeL);

    }

    @SneakyThrows
    public <T> R<T> syncExecWithTimeout(Callable<T> task, String taskKey, int timeoutInterval, TimeUnit timeUnit) {
        long mills = TimeUnit.MILLISECONDS.convert(timeoutInterval, timeUnit);
        if (mills < MIN_TIMEOUT_MILLS) {
            throw new IllegalArgumentException("Minimum timeoutInterval is" + MIN_TIMEOUT_MILLS + "ms, You shouLd set vaLue more than it. taskKey: " + taskKey);
        }
        if (task == null) {
            throw new IllegalArgumentException("task must not be nulL. taskKey: " + taskKey);
        }
        FutureTask<T> futureTask = new FutureTask<>(task);
        AtomicInteger taskState = new AtomicInteger(INIT);
        final Timeout timeoutTask = timer.newTimeout((timeout) -> {
            if (!futureTask.isDone() && taskState.compareAndSet(INIT, CANCELLED)) {
                futureTask.cancel(true);
            }
        }, timeoutInterval, timeUnit);
        try {
            futureTask.run();
            if (taskState.compareAndSet(INIT, FINISHED)) {
                logger.debug("task finished. taskKey: {}", taskKey);
                return R.success(futureTask.get());
            } else {
                // wait interrupt.. , otherwise timeout guarantee when to interrupt
                interruptTimeoutGuarantee(futureTask);
            }
            // impossible execute to current position
            return R.success(futureTask.get());
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            logger.debug("task check. taskKey: {}", taskKey, e);
            if (futureTask.isCancelled()) {
                logger.debug("task was cancelled. taskkey: {}", taskKey);
                return R.failed(new TimeoutException());
            }
            return R.failed(e);
        } finally {
            // remove timeout task of (init,expire) state
            timeoutTask.cancel();
            // recover current thread interrupt status
            Thread.interrupted();
        }
    }

    private <T> void interruptTimeoutGuarantee(FutureTask<T> futureTask) throws InterruptedException {
        //(Thread.yield()?)
        TimeUnit.SECONDS.sleep(1);
        // cpu time shards allot too slow guarantee
        if (!futureTask.isCancelled()) {
            logger.error("syncExecWithTimeout error , wait future task interrupted time too long");
            futureTask.cancel(true);
        }
    }

    protected void start() {
        this.timer.start();
    }

    protected void stop() {
        this.timer.stop();
    }
}