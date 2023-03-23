package com.lyon.easy.async.task.core.strategy;

import cn.hutool.core.date.SystemClock;

/**
 * @author Lyon
 */
public class DefaultTaskAcquireStrategy implements TaskAcquireStrategy {

    @Override
    public long getNextTimestamp(long intervalTimestamp) {
        return SystemClock.now() + intervalTimestamp;
    }

    @Override
    public long getCurrentTimestamp(long intervalTimestamp) {
        return SystemClock.now() + intervalTimestamp * 2;
    }

    @Override
    public String type() {
        return "default";
    }
}
