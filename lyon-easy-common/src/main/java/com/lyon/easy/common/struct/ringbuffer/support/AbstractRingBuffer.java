package com.lyon.easy.common.struct.ringbuffer.support;

import com.lyon.easy.common.struct.ringbuffer.RingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Lyon
 */
@SuppressWarnings("unchecked")
@Slf4j
public abstract class AbstractRingBuffer<E> implements RingBuffer<E> {

    protected final Object[] buffer;

    /**
     * ring-buffer capacity
     */
    protected final int capacity;

    /**
     * next writable
     */
    protected volatile int rear;

    /**
     * next readable
     */
    protected volatile int front;

    /**
     * use capacity
     */
    protected volatile int size;

    /**
     * maximum capacity
     */
    protected static final int MAXIMUM_CAPACITY = 1 << 30;


    public AbstractRingBuffer(int capacity) {
        if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        }
        int reCapacity = tableSizeFor(capacity);
        this.capacity = reCapacity;
        if (log.isDebugEnabled()) {
            log.debug("init ring-buffer struct , origin capacity [{}] re-capacity[{}]", capacity, reCapacity);
        }
        // init ring-buffer
        this.buffer = new Object[reCapacity];
    }

    protected boolean enqueue(E data) {
        if (isFull()) {
            if (log.isDebugEnabled()) {
                log.info("ring-buffer is full.");
            }
            return false;
        }
        doEnqueue(data);
        return true;
    }

    protected void doEnqueue(E data) {
        final int slot = (capacity - 1) & rear + 1;
        if (log.isDebugEnabled()) {
            log.debug("put slot.. front[{}]  next slot[{}]", rear, slot);
        }
        buffer[rear] = data;
        rear = slot;
        sizeForIncrease();
    }

    protected E dequeue() {
        if (isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("ring-buffer is free.");
            }
            return null;
        }
        return doDequeue();
    }

    protected E doDequeue() {
        int slot = (capacity - 1) & front + 1;
        if (log.isDebugEnabled()) {
            log.debug("take slot.. rear[{}]  next slot[{}]", front, slot);
        }
        final Object data = buffer[front];
        buffer[front] = null;
        front = slot;
        sizeForDecrease();
        return (E) data;
    }

    protected boolean isFull() {
        return rear == front && size > 0;
    }

    protected boolean isEmpty() {
        return rear == front && size == 0;
    }

    @SuppressWarnings("TypeParameterHidesVisibleType")
    private class Iter<E> implements Iterator<E> {

        private int iterSize = 0;
        private int cursor = front;

        @Override
        public boolean hasNext() {
            return iterSize < size;
        }

        @Override
        public E next() {
            final Object data = buffer[cursor];
            cursor = (capacity - 1) & cursor + 1;
            iterSize++;
            return (E) data;
        }
    }

    /**
     * increment size
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    protected void sizeForIncrease() {
        size++;
    }

    /**
     * decrement size
     */
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    protected void sizeForDecrease() {
        size--;
    }


    public Iterator<E> iterator() {
        return new Iter<>();
    }

    public int writableSize() {
        return capacity - size;
    }

    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.buffer);
    }

    /**
     * Returns a power of two size for the given target capacity.
     */
    protected int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
}
