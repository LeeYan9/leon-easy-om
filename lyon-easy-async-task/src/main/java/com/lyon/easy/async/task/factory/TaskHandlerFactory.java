package com.lyon.easy.async.task.factory;

import com.lyon.easy.async.task.handler.TaskHandler;

/**
 * @author Lyon
 */
public interface TaskHandlerFactory {


    /**
     * 获取任务
     *
     * @param taskAddress 任务地址
     * @return 批任务
     */
    TaskHandler getNonNullHandler(String taskAddress);
}
