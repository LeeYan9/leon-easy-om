package com.lyon.easy.async.task.dal.dataobject.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyon.easy.async.task.config.mybatis.handler.InEnumTypeHandler;
import com.lyon.easy.async.task.dal.dataobject.extend.BaseDO;
import com.lyon.easy.async.task.enums.ExecStatus;
import com.lyon.easy.async.task.enums.IdcEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

/**
 * @author Lyon
 */
@SuppressWarnings({"jol", "DanglingJavadoc"})
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sub_task")
public class SubTaskDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchTaskId;

    /**
     * 任务编号
     */
    private String jobNo;

    /**
     * 任务参数
     */
    private String param;

    /**
     * 任务地址
     * bean://{{beanName}}
     * class://{{className}}
     */
    private String taskAddress;

    /**
     * 任务组：批处理组件通过所属组执行器进行打捞，执行
     */
    private String groupName;

    /**
     * 消费机房类型
     */
//    @TableField(value = "idc_type",typeHandler = InEnumTypeHandler.class)
    private IdcEnum idcType;

    /**
     * 指定机房
     */
    private String idc;

    /**
     * InEnumTypeHandler
     */
//    @TableField(typeHandler = EnumTypeHandler.class)
//    @TableField(value = "exec_status",typeHandler = InEnumTypeHandler.class)
    private ExecStatus execStatus;

    /**
     * 执行器所有人
     */
    private String owner;

    /**
     * 执行机器
     */
    private String clientId;

    /**
     * 锁状态[1:已锁,0:无锁]
     */
    private Integer lockStatus;

    /**
     * 锁失效时间
     */
    private LocalDateTime lockExpireAt;

    /**
     * 执行结果
     */
    private String result;


    /**
     * 消费机房标识
     */
//    private String idcIdentifier;

}