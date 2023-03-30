package com.lyon.easy.common.struct.ringbuffer;

import com.lyon.easy.common.struct.ringbuffer.support.AbstractRingBuffer;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Lyon
 */
@SuppressWarnings({"UnusedReturnValue", "unused", "AlibabaLockShouldWithTryFinally", "RedundantArrayCreation"})
@Slf4j
public class ConcurrentRingBuffer<E> extends AbstractRingBuffer<E> {


    private final Lock writeLock = new ReentrantLock();
    private final Lock readLock = new ReentrantLock();

    private static final long SIZE_OFFSET;

    /** setup to use Unsafe.compareAndSwapInt for updates*/
    private static final Unsafe UNSAFE;
    static {
        try {
            // bypass security checker , to use Unsafe object operate property 'size'
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor(new Class<?>[0]);
            constructor.setAccessible(true);
            UNSAFE = constructor.newInstance(new Object[0]);
            // access memory address offset for property "size"
            SIZE_OFFSET = UNSAFE.objectFieldOffset
                    (ConcurrentRingBuffer.class.getSuperclass().getDeclaredField("size"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public ConcurrentRingBuffer(int capacity) {
        super(capacity);
    }

    @Override
    public boolean put(E data) {
        final Lock writeLock = this.writeLock;
        try {
            writeLock.lockInterruptibly();
            return enqueue(data);
        } catch (Exception e) {
            log.error("ring-buffer put error ", e);
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    @Override
    public E take() {
        return pull();
    }

    public E pull() {
        final Lock readLock = this.readLock;
        try {
            readLock.lockInterruptibly();
            return dequeue();
        } catch (Exception e) {
            log.error("ring-buffer pull error", e);
        } finally {
            readLock.unlock();
        }
        return null;
    }

    @Override
    protected synchronized void sizeForIncrease() {
        UNSAFE.getAndAddInt(this, SIZE_OFFSET, 1);
    }

    @Override
    protected synchronized void sizeForDecrease() {
        UNSAFE.getAndAddInt(this, SIZE_OFFSET, -1);
    }
}
