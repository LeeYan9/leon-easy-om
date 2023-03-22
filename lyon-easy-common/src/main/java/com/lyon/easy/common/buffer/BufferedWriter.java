package com.lyon.easy.common.buffer;

/**
 * 缓存区写入接口定义
 * @author Lyon
 */
public interface BufferedWriter<T> {

    /**
     * write data to buffer pool
     * @param data data
     */
    void write(T data);
}
