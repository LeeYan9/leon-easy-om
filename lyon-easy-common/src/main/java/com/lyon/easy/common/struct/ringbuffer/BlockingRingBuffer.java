package com.lyon.easy.common.struct.ringbuffer;

import com.lyon.easy.common.struct.ringbuffer.support.AbstractRingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Lyon
 */
@SuppressWarnings({"UnusedReturnValue", "unused", "AlibabaLockShouldWithTryFinally"})
@Slf4j
public class BlockingRingBuffer<E> extends AbstractRingBuffer<E> {

    public ReentrantLock mutexLock = new ReentrantLock();

    public Condition notEmpty = mutexLock.newCondition();
    public Condition notFull = mutexLock.newCondition();

    public BlockingRingBuffer(int capacity) {
        super(capacity);
    }

    @Override
    public boolean put(E data) {
        ReentrantLock lock = this.mutexLock;
        try {
            lock.lockInterruptibly();

            if (isFull()) {
                if (log.isDebugEnabled()) {
                    log.debug("ring-buffer is full. try to be wait release element , condition wait count {}", lock.getWaitQueueLength(notFull) + 1);
                }
                notFull.await();
            }
            doEnqueue(data);
            notEmpty.signal();
            return true;
        } catch (Exception e) {
            log.error("ring-buffer put error ", e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public E take() {
        final ReentrantLock lock = this.mutexLock;
        try {
            lock.lockInterruptibly();
            if (isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("ring-buffer is free. try to be wait enqueue element , condition wait count {}", lock.getWaitQueueLength(notEmpty) + 1);
                }
                notEmpty.await();
            }
            E data = doDequeue();
            notFull.signal();
            return data;
        } catch (Exception e) {
            log.error("ring-buffer pull error", e);
            return null;
        } finally {
            lock.unlock();
        }
    }
}
