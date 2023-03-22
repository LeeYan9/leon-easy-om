package com.lyon.easy.async.task.data;

import lombok.Data;

import java.util.List;

/**
 * 批处理任务
 *
 * @author Lyon
 */
@Data
public class BatchTaskState {

    /**
     * 批任务执行状态信息
     */
    private TaskExecStateDesc<BatchTask> batchTask;

    /**
     * 子任务执行状态信息
     */
    private List<SubTaskState> subTasks;

}
