package com.lyon.easy.common.future;

import cn.hutool.core.thread.ThreadUtil;
import io.netty.util.HashedWheelTimer;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Lyon
 */
public class FutureTaskTest {

    @SneakyThrows
    public static void main(String[] args) {
        testFutureTaskExecTimeDiffs();
    }

    private static void testFutureTaskExecTimeDiffs() throws InterruptedException, ExecutionException, TimeoutException {
        final FutureTask<String> task = new FutureTask<>(() -> {
            System.out.println("task before");
            ThreadUtil.safeSleep(5000);
            System.out.println("task after");
            return "task executed";
        });
        task.run();
        System.out.println("task executed after handle");
        System.out.println(task.get(2, TimeUnit.SECONDS));
    }

    public static void testCancelExceptionWhenFutureTaskComplete(){
        //define task
        final FutureTask<String> task = new FutureTask<>(() -> {
            System.out.println("task before");
            ThreadUtil.safeSleep(5000);
            System.out.println("task after");
            return "task executed";
        });
        // define timer and timeout-task
        task.run();
    }


}
