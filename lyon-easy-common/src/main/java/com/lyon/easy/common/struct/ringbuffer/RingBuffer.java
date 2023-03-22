package com.lyon.easy.common.struct.ringbuffer;

import com.lyon.easy.common.struct.Struct;

/**
 * @author Lyon
 */
@SuppressWarnings("UnusedReturnValue")
public interface RingBuffer<E> extends Struct {

    /**
     * 添加元素
     * @param data 数据
     * @return 结果
     */
    boolean put(E data);


    /**
     * 获取元素
     * @return 结果
     */
    E take();
}
