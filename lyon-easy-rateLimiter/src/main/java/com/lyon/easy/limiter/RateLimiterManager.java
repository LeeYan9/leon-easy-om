package com.lyon.easy.limiter;

import com.lyon.easy.limiter.core.ConcurrencySlideWindowRateLimiter;
import com.lyon.easy.limiter.core.RateLimiter;
import com.lyon.easy.limiter.core.SlideWindowRateLimiter;
import com.lyon.easy.limiter.helper.LimitCounterInvalidReactor;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author Lyon
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class RateLimiterManager {

    @Getter
    private final Map<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();
    @Getter
    private Map<String, RateLimiter> observerMap;

    @Getter
    public long secondsPerWindow;
    @Getter
    public long maxRequestsPreWindow;

    private final boolean concurrency;

    private Supplier<RateLimiter> supplier = () -> new SlideWindowRateLimiter(secondsPerWindow, maxRequestsPreWindow);

    public LimitCounterInvalidReactor helper;

    public RateLimiterManager(Boolean concurrency, boolean observeEnable,
                              long secondsPerWindow, long maxRequestsPreWindow) {
        this.secondsPerWindow = secondsPerWindow;
        this.maxRequestsPreWindow = maxRequestsPreWindow;
        if (observeEnable) {
            observerMap = new ConcurrentHashMap<>();
        }
        this.concurrency = concurrency;
        if (this.concurrency) {
            this.helper = new LimitCounterInvalidReactor(this);
            this.supplier = () -> new ConcurrencySlideWindowRateLimiter(secondsPerWindow, maxRequestsPreWindow);
        }
    }

    public boolean acquire(String identifier) {
        RateLimiter rateLimiter = observerMap.compute(identifier, (key, value) -> {
            if (Objects.isNull(value)) {
                value = supplier.get();
            }
            return value;
        });
        // if enabled observe then record any access
        if (Objects.nonNull(observerMap)) {
            RateLimiter observer = observerMap.compute(identifier, (key, value) -> {
                if (Objects.isNull(value)) {
                    value = supplier.get();
                }
                return value;
            });
            observer.increase();
        }
        return rateLimiter.tryAcquire();
    }

    public void statInfo() {

    }


}
