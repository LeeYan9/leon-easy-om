package com.lyon.easy.async.task;

import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.BatchTaskState;
import com.lyon.easy.async.task.data.SubTaskState;

/**
 * @author Lyon
 */
@SuppressWarnings("unused")
public interface BatchTaskTemplate {

    /**
     * 添加批处理任务
     * @param batchTask task
     */
    void submitTask(BatchTask batchTask);

    /**
     * 取消任务，正在执行的任务不会取消
     * @param batchTaskId 批任务id
     * @return 是否可以取消，取消是异步操作
     */
    boolean cancelTask(Long batchTaskId);

    /**
     * 中断任务，正在执行的任务也会取消
     * @param batchTaskId 批任务id
     * @return 是否可以中断，中断是异步操作
     */
    boolean interruptTask(Long batchTaskId);

    /**
     * 批任务状态信息
     * @param batchNo 批次号
     * @return 结果
     */
    BatchTaskState getBatchTaskState(String batchNo);

    /**
     * 子任务状态信息
     * @param jobNo 任务编号
     * @return 结果
     */
    SubTaskState getSubTaskState(String jobNo);

}
