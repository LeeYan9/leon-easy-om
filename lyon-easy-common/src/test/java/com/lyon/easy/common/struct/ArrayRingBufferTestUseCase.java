package com.lyon.easy.common.struct;


import com.lyon.easy.common.struct.ringbuffer.ArrayRingBuffer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Lyon
 */
public class ArrayRingBufferTestUseCase {


    @Test
    public void testRingBufferIter() {
        ArrayRingBuffer<Integer> ringBuffer = new ArrayRingBuffer<>(10);
        for (int i = 1; i <= 17; i++) {
            ringBuffer.put(i);
        }
        for (int i = 1; i <= 5; i++) {
            ringBuffer.pull();
        }
        final Iterator<Integer> iterator = ringBuffer.iterator();
        System.out.println("ring-buffer:size "+ringBuffer.size() +"  writableSize:"+ ringBuffer.writableSize());
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

    }

    @Test
    public void testRingBuffer() {
        ArrayRingBuffer<Integer> ringBuffer = new ArrayRingBuffer<>(10);
        for (int i = 1; i <= 17; i++) {
            ringBuffer.put(i);
        }

        final ArrayList<Integer> intArr = new ArrayList<>();
        for (int i = 1; i <= 17; i++) {
            final Integer pull = ringBuffer.pull();
            if (pull != null) {
                intArr.add(pull);
            }
        }
        System.out.println("pull data list " + intArr);
        System.out.println("ring-buffer data list " + ringBuffer);
    }

}
