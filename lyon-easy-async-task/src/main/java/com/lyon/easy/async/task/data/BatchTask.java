package com.lyon.easy.async.task.data;

import com.lyon.easy.async.task.enums.IdcEnum;
import lombok.Data;

import java.util.List;

/**
 * 批处理任务
 *
 * @author Lyon
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
@Data
public class BatchTask {

    /**
     * 任务批次号
     */
    private String batchNo;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务显示名称
     */
    private String displayName;

    /**
     * 来源
     */
    private String src;

    /**
     * 任务组：批处理组件通过所属组执行器进行打捞，执行
     */
    private String groupName;

    /**
     * 消费机房类型
     */
    private IdcEnum idcType;

    /**
     * 消费机房标识
     *
     * @deprecated
     */
//    private String idcIdentifier;

    private List<DependOnBatchTask> dependOns;

    private List<SubTask> subTasks;


}
