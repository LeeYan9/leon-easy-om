package com.lyon.easy.async.task.data;

import com.lyon.easy.async.task.enums.IdcEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotBlank(message = "任务批次号不能为空")
    private String batchNo;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    /**
     * 任务显示名称
     */
    @NotBlank(message = "任务显示名称不能为空")
    private String displayName;

    /**
     * 来源
     */
    @NotBlank(message = "任务来源不能为空")
    private String src;

    /**
     * 任务组：批处理组件通过所属组执行器进行打捞，执行
     */
    @NotBlank(message = "任务组不能为空")
    private String groupName;

    /**
     * 消费机房类型
     */
    @NotNull(message = "任务消费机房类型不能为空")
    private IdcEnum idcType;

    /**
     * 消费机房标识
     *
     * @deprecated
     */
//    private String idcIdentifier;

    private List<DependOnBatchTask> dependOns;

    @NotEmpty(message = "任务明细列表不能为空")
    private List<SubTask> subTasks;


}
