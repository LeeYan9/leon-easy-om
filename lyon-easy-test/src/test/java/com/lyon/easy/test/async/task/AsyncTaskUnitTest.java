package com.lyon.easy.test.async.task;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.lyon.easy.async.task.BatchTaskTemplate;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.BatchTaskState;
import com.lyon.easy.async.task.enums.IdcEnum;
import com.lyon.easy.test.ApplicationServer;
import com.lyon.easy.test.SimpleLongExecTaskHandler;
import com.lyon.easy.test.async.task.core.AsyncTaskExecutorConfig;
import com.lyon.easy.test.async.task.util.GenerateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * @author Lyon
 */
//@ActiveProfiles("async-task-unit")
@SpringBootTest(classes = ApplicationServer.class)
@Import({AsyncTaskExecutorConfig.class})
public class AsyncTaskUnitTest {

    @Resource
    private BatchTaskTemplate batchTaskTemplate;


    @Test
    public void testCompare() {
        final BatchTask batchTask = GenerateUtil.generateExampleBatchTask("order-stat", IdcEnum.ANY, 2, SimpleLongExecTaskHandler.class);
        batchTaskTemplate.submitTask(batchTask);

        BatchTaskState batchTaskState;
        while (null == (batchTaskState = batchTaskTemplate.getBatchTaskState(batchTask.getBatchNo()))) {
            Thread.yield();
        }
        System.out.println(JSONUtil.toJsonStr(batchTaskState));
        ThreadUtil.safeSleep(10000);
    }

}
