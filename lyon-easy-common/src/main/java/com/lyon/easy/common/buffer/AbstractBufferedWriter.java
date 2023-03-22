package com.lyon.easy.common.buffer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存区写入模板实现
 * @author Lyon
 */
@SuppressWarnings({"unused"})
@Slf4j
public abstract class AbstractBufferedWriter<T> implements BufferedWriter<T> {

    public AbstractBufferedWriter(ThreadPoolExecutor executor, int poolSizePerBatch,
                                  Duration flushGuaranteeIntervals, CallBackListener... callBackListeners) {
        this.executor = executor;
        this.poolSizePerBatch = poolSizePerBatch;
        this.flushGuaranteeIntervals = flushGuaranteeIntervals;
        if (ArrayUtil.isNotEmpty(callBackListeners)) {
            this.callBackListeners = new CopyOnWriteArrayList<>(callBackListeners);
        }
    }

    /**
     * 缓存区写入执行器
     * handler for buffer pool
     */
    protected ThreadPoolExecutor executor;

    /**
     * <p>太长时间没有刷新时，刷新保证执行器</p>
     * too long time un-flush , that flush guarantee
     */
    protected ScheduledThreadPoolExecutor flushGuaranteeExecutor;

    /**
     * <p>刷新动作的偏移位监听器的执行器</p>
     * listener for buffer pool offset flush
     */
    protected ScheduledThreadPoolExecutor callbackListenerExecutor;

    /**
     * pool size per batch
     */
    protected int poolSizePerBatch;

    /**
     * flush guarantee intervals
     */
    protected Duration flushGuaranteeIntervals;

    protected final AtomicBoolean writeRunning = new AtomicBoolean(false);

    /**
     * last once write to buffer pool time-stamp
     */
    private volatile long lastWriteTimeMills = System.currentTimeMillis();

    /**
     * data buffer pool
     */
    private volatile List<T> buffer = new CopyOnWriteArrayList<>();

    /**
     * callback for offset of listener
     */
    private List<CallBackListener> callBackListeners;

    /**
     * synchronized mutex for buffer pool exchange snapshot pool
     */
    private final Object mutex = new Object();

    @Getter
    protected AtomicLong bufferOffset = new AtomicLong(0);
    @Getter
    protected AtomicLong flushedOffset = new AtomicLong(0);


    /**
     * write data to buffer
     * @param data data
     */
    @Override
    public void write(T data) {
        // init of lazy
        lazyInit();
        // add data to buffer . when buffer size less poolSizePerBatch then return.
        buffer.add(data);
        bufferOffset.incrementAndGet();
        if (buffer.size() < poolSizePerBatch) {
            return;
        }
        // prevent high concurrency of empty list assign buffer
        exchangeAndFlush(false);
    }

    /**
     * real write action of data to server
     *
     * @param data data
     */
    abstract void flush(List<T> data);


    protected void doWork(List<T> buffer) {
        try {
            flush(buffer);
        } catch (Exception e) {
            log.error("bufferedWriter flush data error", e);
        } finally {
            flushedOffset.addAndGet(buffer.size());
            lastWriteTimeMills = System.currentTimeMillis();
        }
    }

    private void exchangeAndFlush(boolean force) {
        List<T> snapshot;
        synchronized (mutex) {
            if (buffer.size() < poolSizePerBatch && !force) {
                return;
            }
            if (buffer.size() == 0) {
                return;
            }
            //COW exchange data
            snapshot = this.buffer;
            this.buffer = new CopyOnWriteArrayList<>();
        }
        executor.submit(new FlushWorker(snapshot));
    }

    protected void lazyInit() {
        if (writeRunning.get()) {
            return;
        }
        final boolean compareAndSet = writeRunning.compareAndSet(false, true);
        // enable
        if (compareAndSet) {
            // flushGuarantee worker client
            flushGuaranteeExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("flush-timeout-", true));
            flushGuaranteeExecutor.scheduleWithFixedDelay(new TimeoutGuaranteeWorker(), 0, flushGuaranteeIntervals.toMillis(), TimeUnit.MILLISECONDS);
            // callback listener worker client
            callbackListenerExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("callback-action-listener-", true));
            callbackListenerExecutor.scheduleWithFixedDelay(new CallbackListenerWorker(), 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    @AllArgsConstructor
    class FlushWorker implements Runnable {

        private final List<T> buffer;

        @Override
        public void run() {
            doWork(buffer);
        }
    }

    /**
     * too long not flush, then flush guarantee
     */
    class TimeoutGuaranteeWorker implements Runnable {
        @Override
        public void run() {
            tryTimeoutGuaranteeWorker();
        }

        public synchronized void tryTimeoutGuaranteeWorker() {
            boolean enabledGuarantee = buffer.size() > 0 && System.currentTimeMillis() > lastWriteTimeMills + flushGuaranteeIntervals.toMillis();
            if (enabledGuarantee) {
                exchangeAndFlush(true);
                log.info("bufferedWriter timeout guarantee worker client .. try flush data");
            }
        }
    }

    class CallbackListenerWorker implements Runnable {
        @Override
        public void run() {
            listener();
        }

        public synchronized void listener() {
            // try shutdown that current worker
            if (callBackListeners.isEmpty()) {
                log.info("bufferedWriter callback try close ");
                callbackListenerExecutor.shutdown();
                return;
            }
            // execute
            List<CallBackListener> usedCallbacks = new ArrayList<>();
            for (CallBackListener callBackListener : callBackListeners) {
                final long flushedOffset = AbstractBufferedWriter.this.flushedOffset.get();
                if (flushedOffset >= callBackListener.getOffset()) {
                    usedCallbacks.add(callBackListener);
                    callBackListener.getCallBack().action();
                    log.info("bufferedWriter callback execute offset[{}] listenerOffset[{}].. ", flushedOffset, callBackListener.offset);
                }
            }
            // clear expired callback-listeners
            if (CollUtil.isNotEmpty(usedCallbacks)) {
                callBackListeners.removeAll(usedCallbacks);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class CallBackListener {
        /**
         * offset
         */
        private final Long offset;
        /**
         * callback action
         */
        private CallBack callBack;
    }

    @FunctionalInterface
    public interface CallBack {
        /**
         * callback action
         */
        void action();
    }

}
