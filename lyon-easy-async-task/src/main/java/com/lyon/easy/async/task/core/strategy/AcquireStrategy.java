package com.lyon.easy.async.task.core.strategy;

/**
 * @author Lyon
 */
public interface AcquireStrategy {

    /**
     * get next
     * @param intervalTimestamp interval timestamp
     * @return next
     */
    long acquireNextTimestamp(long intervalTimestamp);

    /**
     * get current
     * @param intervalTimestamp interval timestamp
     * @return current
     */
    long acquireCurrentTimestamp(long intervalTimestamp);

    /**
     * type
     * @return type
     */
    String type();

}
