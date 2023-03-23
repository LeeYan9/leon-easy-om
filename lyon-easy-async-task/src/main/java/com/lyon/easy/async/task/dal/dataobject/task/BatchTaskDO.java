package com.lyon.easy.async.task.dal.dataobject.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.lyon.easy.async.task.config.mybatis.handler.InEnumTypeHandler;
import com.lyon.easy.async.task.dal.dataobject.extend.BaseDO;
import com.lyon.easy.async.task.enums.ExecStatus;
import com.lyon.easy.async.task.enums.IdcEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lyon
 */
@SuppressWarnings({"jol", "AlibabaRemoveCommentedCode"})
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("batch_task")
public class BatchTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
//    @TableField(value = "idc_type",typeHandler = MybatisEnumTypeHandler.class)
    private IdcEnum idcType;

    /**
     * 执行状态
     */
//    @TableField(value = "exec_status",typeHandler = MybatisEnumTypeHandler.class)

    private ExecStatus execStatus;

    /**
     * 消费机房标识
     */
//    private String idcIdentifier;

    private String dependOns;
}
