package com.lyon.easy.limiter.counter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lyon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecondsCounter implements Counter {
    /**
     * 当前秒
     */
    private long epochSeconds;

    /**
     * 计数
     */
    private volatile long value;

    /**
     * setup to use Unsafe.compareAndSwapInt for updates
     */
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static final long VALUE_OFFSET;

    static {
        try {
            VALUE_OFFSET = UNSAFE.objectFieldOffset
                    (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    @Override
    public void increase() {
        UNSAFE.getAndAddInt(this, VALUE_OFFSET, 1);
    }

    @Override
    public Long getEpochTime() {
        return this.epochSeconds;
    }

    @Override
    public Long getValue() {
        return value;
    }
}
