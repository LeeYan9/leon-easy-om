package com.lyon.easy.async.task.data;

import com.lyon.easy.async.task.enums.ExecStatus;
import lombok.Data;

/**
 * @author lyon
 */
@Data
public class TaskExecStateDesc<T> {

    /**
     * 任务源信息
     */
    private T data;

    /**
     * 执行状态
     */
    private ExecStatus status;

    /**
     * 执行结果
     */
    private String resultMsg;

}