package com.lyon.easy.async.task.protocol.task;

import com.lyon.easy.async.task.handler.TaskHandler;

/**
 * @author Lyon
 */
public interface TaskProtocol {

    /**
     * 获取任务
     *
     * @param taskAddress 任务地址
     * @return 批任务标识
     */
    TaskHandler getHandler(String taskAddress);

    /**
     * 协议支持判断
     * @param taskAddress 任务地址
     * @return flag
     */
    boolean support(String taskAddress);
}
