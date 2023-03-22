package com.lyon.easy.limiter.helper;

import com.lyon.easy.limiter.RateLimiterManager;
import com.lyon.easy.limiter.core.RateLimiter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lyon
 */
public class LimitCounterInvalidReactor {

    private final RateLimiterManager limiterManager;

    private final AtomicInteger atomicInteger = new AtomicInteger();

    //    private int defaultAllotPerThread = 100;

    public LimitCounterInvalidReactor(RateLimiterManager limiterManager) {
        this.limiterManager = limiterManager;
        int coreThreads = 1;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(coreThreads, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "LimitCounterInvalidHelper-Thread-" + atomicInteger.incrementAndGet());
            }
        });
        scheduledThreadPoolExecutor.schedule(new CounterInvalidCheckerWorkerClient(), 1, TimeUnit.SECONDS);
    }

    void check() {
        final Set<Map.Entry<String, RateLimiter>> entries = limiterManager.getLimiterMap().entrySet();
        for (Map.Entry<String, RateLimiter> entry : entries) {
            final RateLimiter rateLimiter = entry.getValue();
            // ignore unused limiter
            if (rateLimiter.total()>0) {
                rateLimiter.clearInExpire();
            }

        }
    }

    /**
     * laster optimal -> can per worker handle partion data
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    class CounterInvalidCheckerWorkerClient implements Runnable {

        @Override
        public void run() {
            check();
        }
    }
}
