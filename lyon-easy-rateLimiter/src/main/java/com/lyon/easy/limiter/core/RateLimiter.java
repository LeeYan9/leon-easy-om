package com.lyon.easy.limiter.core;

/**
 * @author Lyon
 */
public interface RateLimiter {

    /**
     *  increase of current seconds
     */
    void increase();

    /**
     * get total
     * @return total
     */
    long total();

    /**
     * tryAcquire
     * @return result
     */
    boolean tryAcquire();

    /**
     * clear counter of request count the invalid seconds
     */
    void clearInExpire();
}
