package com.lyon.easy.common.buffer;

import cn.hutool.core.thread.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Lyon
 */
@SuppressWarnings({"AlibabaThreadShouldSetName", "jol"})
@Slf4j
public class ExampleBufferedWriter extends AbstractBufferedWriter<Product> {

    public KafkaClientExample<Product> clientExample = new KafkaClientExample<>();

    public ExampleBufferedWriter(ThreadPoolExecutor executor, int poolSizePerBatch,
                                 Duration flushInterval,CallBackListener listener) {
        super(executor, poolSizePerBatch, flushInterval,listener);
    }

    @Override
    public void flush(List<Product> list) {
        list.forEach(clientExample::send);
        log.info("flished data.. size [{}]", list.size());
        ThreadUtil.safeSleep(200);
    }

    @SneakyThrows
    public static void main(String[] args) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        BufferedWriter<Product> bufferedWriter = new ExampleBufferedWriter(executor, 100, Duration.ofSeconds(2),
                new CallBackListener(201L,() -> System.out.println("回调执行")));
        for (int i =0 ; i< 9 ; i ++) {
            new Thread(() -> {
                for (int j = 1; j < 100L; j++) {
                    bufferedWriter.write(new Product(j));
                }
            }).start();
        }
        ThreadUtil.safeSleep(100000);
    }

}

@Data
@AllArgsConstructor
class Product {
    private int id;
}

class KafkaClientExample<T> {
    public void send(T data) {
//        System.out.println(data.toString());
    }
}

