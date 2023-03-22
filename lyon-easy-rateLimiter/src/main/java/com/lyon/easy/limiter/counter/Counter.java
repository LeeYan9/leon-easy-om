package com.lyon.easy.limiter.counter;

/**
 * @author Lyon
 */
public interface Counter {

    /**
     * 递增次数
     */
    void increase();

    /**
     * 获取计数结果
     * @return 计数结果
     */
    Long getValue();

    /**
     * key
     * @return key
     */
    Long getEpochTime();

}