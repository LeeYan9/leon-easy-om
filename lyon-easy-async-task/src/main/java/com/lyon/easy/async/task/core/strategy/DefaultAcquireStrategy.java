package com.lyon.easy.async.task.core.strategy;

import cn.hutool.core.date.SystemClock;

/**
 * @author Lyon
 */
public class DefaultAcquireStrategy implements AcquireStrategy {

    @Override
    public long acquireNextTimestamp(long intervalTimestamp) {
        return SystemClock.now() + intervalTimestamp;
    }

    @Override
    public long acquireCurrentTimestamp(long intervalTimestamp) {
        return SystemClock.now() + intervalTimestamp * 2;
    }

    @Override
    public String type() {
        return "default";
    }
}
