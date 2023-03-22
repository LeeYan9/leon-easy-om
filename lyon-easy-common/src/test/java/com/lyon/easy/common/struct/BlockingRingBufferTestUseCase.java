package com.lyon.easy.common.struct;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.lyon.easy.common.struct.ringbuffer.BlockingRingBuffer;
import com.lyon.easy.common.struct.ringbuffer.RingBuffer;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lyon
 */
public class BlockingRingBufferTestUseCase {

    @Test
    public void testRingBuffer() {
        RingBuffer<Integer> ringBuffer = new BlockingRingBuffer<>(10);
        final AtomicInteger putCounter = new AtomicInteger();
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {
                putCounter.incrementAndGet();
                ringBuffer.put(RandomUtil.randomInt());
            }).start();
        }
        ThreadUtil.safeSleep(1000);
        System.out.println("==================put action total count" + putCounter.get());

        final AtomicInteger taskCounter = new AtomicInteger();
        for (int i = 1; i <= 50; i++) {
            final Thread thread = new Thread(() -> {
                taskCounter.incrementAndGet();
                ringBuffer.take();
            });
            thread.start();
        }
        ThreadUtil.safeSleep(1000);
        System.out.println("==================task action total count " + taskCounter.get());
    }
}
