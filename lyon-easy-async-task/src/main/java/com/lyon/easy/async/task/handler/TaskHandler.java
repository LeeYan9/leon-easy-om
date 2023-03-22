package com.lyon.easy.async.task.handler;

/**
 * @author Lyon
 */
public interface TaskHandler {

    /**
     * 任务执行
     * @param param 参数
     * @param <T> 泛型
     * @return 任务结果
     */
    <T> T execute(String param);

}
