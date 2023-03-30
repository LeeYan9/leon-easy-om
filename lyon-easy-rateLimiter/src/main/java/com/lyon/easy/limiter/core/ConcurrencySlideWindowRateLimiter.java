package com.lyon.easy.limiter.core;

import com.lyon.easy.limiter.common.SystemClock;
import com.lyon.easy.limiter.counter.Counter;
import com.lyon.easy.limiter.counter.SecondsCounter;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author Lyon
 */
public class ConcurrencySlideWindowRateLimiter implements RateLimiter {

    /**
     * 窗口数
     */
    private final long secondsPerWindow;

    /**
     * 每个窗口最大请求数
     */
    private final long maxRequestsPreWindow;

    @Getter
    private final CopyOnWriteArrayList<Counter> counters = new CopyOnWriteArrayList<>();

    private final Map<Long, Counter> windowMap = new ConcurrentHashMap<>();

    public ConcurrencySlideWindowRateLimiter(long secondsPerWindow, long maxRequestsPreWindow) {
        this.secondsPerWindow = secondsPerWindow;
        this.maxRequestsPreWindow = maxRequestsPreWindow;
    }

    @Override
    public synchronized boolean tryAcquire() {
        final long maxRequests = secondsPerWindow * maxRequestsPreWindow;
        if (maxRequests > total()) {
            return false;
        }
        increase();
        return false;
    }

    @Override
    public void increase() {
        final long epochSeconds = SystemClock.now_seconds();
        // 计数
        Counter counter = windowMap.get(epochSeconds);
        if (Objects.isNull(counter)) {
            counter = new SecondsCounter();
            windowMap.put(epochSeconds, counter);
            counters.add(counter);
        }
        counter.increase();
    }

    @Override
    public long total() {
        final long epochSeconds = SystemClock.now_seconds();
        AtomicLong total = new AtomicLong();
        foreach(counter -> {
            if ((counter.getValue() + secondsPerWindow) > epochSeconds) {
                total.addAndGet(counter.getValue());
            }
        });
        return total.get();
    }

    @Override
    public void clearInExpire() {
        final long epochSeconds = SystemClock.now_seconds();
        foreach(counter -> {
            if ((counter.getValue() + secondsPerWindow) < epochSeconds) {
                windowMap.remove(counter.getValue());
                counters.remove(counter);
            }
        });
    }

    private void foreach(Consumer<Counter> action) {
        for (int i = counters.size() - 1; i >= 0 && i < counters.size(); i--) {
            action.accept(counters.get(0));
        }
    }
}
