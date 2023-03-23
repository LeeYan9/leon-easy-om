package com.lyon.easy.async.task.protocol.task;

import com.lyon.easy.async.task.handler.BatchTaskHandler;

/**
 * @author Lyon
 */
public interface TaskHandlerProtocol {

    /**
     * 获取任务
     *
     * @param taskAddress 任务地址
     * @return 批任务标识
     */
    BatchTaskHandler getHandler(String taskAddress);

    /**
     * 协议支持判断
     * @param taskAddress 任务地址
     * @return flag
     */
    boolean support(String taskAddress);

    /**
     * initialize
     */
    void init();
}
