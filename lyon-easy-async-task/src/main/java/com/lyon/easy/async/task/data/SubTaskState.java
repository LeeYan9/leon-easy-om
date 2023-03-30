package com.lyon.easy.async.task.data;

import lombok.Data;

/**
 * 批处理任务
 *
 * @author Lyon
 */
@Data
public class SubTaskState {

    /**
     * 子任务执行状态信息
     */
    private TaskExecStateDesc<SubTask> subTask;

}
