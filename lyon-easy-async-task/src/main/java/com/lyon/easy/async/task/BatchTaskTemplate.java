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
