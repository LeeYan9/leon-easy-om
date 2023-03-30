package com.lyon.easy.async.task.core.strategy;

/**
 * @author Lyon
 */
public interface TaskAcquireStrategy {

    /**
     * get next
     * @param intervalTimestamp interval timestamp
     * @return next
     */
    long getNextTimestamp(long intervalTimestamp);

    /**
     * get current
     * @param intervalTimestamp interval timestamp
     * @return current
     */
    long getCurrentTimestamp(long intervalTimestamp);

    /**
     * type
     * @return type
     */
    String type();

}
