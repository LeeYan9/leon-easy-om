package com.lyon.easy.async.task.protocol.task;

import com.lyon.easy.async.task.handler.BatchTaskHandler;

/**
 * @author Lyon
 */
@SuppressWarnings("unused")
public interface TaskHandlerRegister {

    /**
     * 任务处理器注册
     *
     * @param batchTaskHandler 任务处理器
     */
    void register(BatchTaskHandler<?> batchTaskHandler);

}
