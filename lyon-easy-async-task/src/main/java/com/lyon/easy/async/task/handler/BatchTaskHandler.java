package com.lyon.easy.async.task.handler;

/**
 * @author Lyon
 */
public interface BatchTaskHandler<T> {

    /**
     * 任务执行
     *
     * @param param 参数
     * @return 任务结果
     */
    T execute(String param);

}
