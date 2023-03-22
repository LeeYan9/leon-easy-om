package com.lyon.easy.async.task.protocol.task;

import com.lyon.easy.async.task.handler.TaskHandler;

/**
 * @author Lyon
 */
public interface TaskHandlerRegister {

    /**
     * 任务处理器注册
     *
     * @param taskHandler 任务处理器
     */
    void register(TaskHandler taskHandler);

}
