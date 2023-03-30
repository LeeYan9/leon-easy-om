package com.lyon.easy.common.struct.ringbuffer;

import com.lyon.easy.common.struct.ringbuffer.support.AbstractRingBuffer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lyon
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@Slf4j
public class ArrayRingBuffer<E> extends AbstractRingBuffer<E> {

    public ArrayRingBuffer(int capacity) {
        super(capacity);
    }

    @Override
    public boolean put(E data) {
        return enqueue(data);
    }

    @Override
    public E take() {
        return pull();
    }

    public E pull() {
        return dequeue();
    }
}
