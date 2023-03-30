package com.lyon.easy.limiter.core;

import com.lyon.easy.limiter.common.SystemClock;
import com.lyon.easy.limiter.counter.Counter;
import com.lyon.easy.limiter.counter.SecondsCounter;
import lombok.Getter;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lyon
 */
public class SlideWindowRateLimiter implements RateLimiter {

    /**
     * 窗口数
     */
    private final long secondsPerWindow;

    /**
     * 每个窗口最大请求数
     */
    private final long maxRequestsPreWindow;

    @Getter
    private final PriorityQueue<Counter> queue = new PriorityQueue<>(64, Comparator.comparing(Counter::getValue));

    private final Map<Long, Counter> windowMap = new ConcurrentHashMap<>();

    public SlideWindowRateLimiter(long secondsPerWindow, long maxRequestsPreWindow) {
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
            windowMap.put(counter.getEpochTime(),counter);
            queue.offer(counter);
        }
        counter.increase();
    }

    @Override
    public long total() {
        final long epochSeconds = SystemClock.now_seconds();
        long total = 0;
        clearInExpire();
        for (Counter window : queue) {
            final Long key = window.getEpochTime();
            if ((key + secondsPerWindow) > epochSeconds) {
                total += window.getValue();
            }
        }
        return total;
    }

    @Override
    public void clearInExpire() {
        final long epochSeconds = SystemClock.now_seconds();
        Counter next;
        while ((next = queue.peek()) != null && (next.getValue() + secondsPerWindow) < epochSeconds) {
            next = queue.poll();
            assert next != null;
            windowMap.remove(next.getEpochTime());
        }
    }
}
