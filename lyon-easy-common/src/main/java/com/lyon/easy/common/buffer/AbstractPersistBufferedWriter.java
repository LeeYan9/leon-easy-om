package com.lyon.easy.common.buffer;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Lyon
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Slf4j
public abstract class AbstractPersistBufferedWriter<T> implements BufferedWriter<T> {

    public AbstractPersistBufferedWriter(ThreadPoolExecutor executor, int poolSizePerBatch) {
        this.executor = executor;
        this.poolSizePerBatch = poolSizePerBatch;
        final ThreadFactory factory = ThreadFactoryBuilder.create().setDaemon(true).setNamePrefix("PersistBuffered-").build();
        innerWorker = factory.newThread(new ClientWorker());
        innerWorker.start();
    }

    protected ThreadPoolExecutor executor;

    protected int poolSizePerBatch;

    private volatile List<T> readBuffer = new ArrayList<>();
    private final List<T> writeBuffer = new CopyOnWriteArrayList<>();

    private final Thread innerWorker;
    private final Object mutex = new Object();

    private long lastWriteTimeMills;
    private long lastFlushTimeMills;

    @Override
    public synchronized void write(T data) {
        readBuffer.add(data);
        lastWriteTimeMills = SystemClock.now();
        if (readBuffer.size() < poolSizePerBatch) {
            return;
        }
        // prevent high concurrency of empty list assign buffer
        List<T> snapshot;
        synchronized (mutex) {
            if (readBuffer.size() < poolSizePerBatch) {
                return;
            }
            //COW exchange data
            snapshot = this.readBuffer;
            this.readBuffer = new ArrayList<>();
            writeBuffer.addAll(snapshot);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @NoArgsConstructor
    class ClientWorker implements Runnable {

        private List<T> buffer = new ArrayList<>();

        @Override
        public void run() {
            while (true) {
                if (writeBuffer.isEmpty()) {
                    ThreadUtil.safeSleep(1000);
                }
                for (T next : writeBuffer) {
                    buffer.add(next);
                    if (buffer.size() >= poolSizePerBatch) {
                        try {
                            doWork(buffer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            buffer = new ArrayList<>();
                        }
                    }
                }
            }
        }
    }

    protected void doWork(List<T> buffer) {
        try {
            flush(buffer);
        } catch (Exception e) {
            log.error("bufferedWriter flush data error", e);
        } finally {
            lastFlushTimeMills = SystemClock.now();
        }
    }

    /**
     * flush data
     *
     * @param list data list
     */
    protected abstract void flush(List<T> list);

}
