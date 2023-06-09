package com.lyon.easy.async.task.data;

import com.lyon.easy.async.task.enums.IdcEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author lyon
 */
@Data
public class SubTask {

    /**
     * 任务编号
     */
    @NotBlank(message = "任务编号不能为空")
    private String jobNo;

    /**
     * 任务参数
     */
    private String param;

    /**
     * 任务组：批处理组件通过所属组执行器进行打捞，执行
     */
    @NotBlank(message = "任务组不能为空")
    private String groupName;

    /**
     * 任务地址
     * bean://{{beanName}}
     * class://{{className}}
     */
    @NotBlank(message = "任务地址")
    private String taskAddress;

    private ExecRecord execRecord;

    @SuppressWarnings("unused")
    @Data
    public static class ExecRecord {

        private Long id;

        private Long batchTaskId;

        /**
         * 消费机房类型
         */
        private IdcEnum idcType;

        /**
         * 指定机房
         */
        private String idc;

        /**
         * 执行器所有人
         */
        private String owner;

        /**
         * 执行机器
         */
        private String clientId;

        /**
         * 锁状态 []
         */
        private Integer lockStatus;

        /**
         * 锁失效时间
         */
        private LocalDateTime lastHeartbeatTime;

        /**
         * 失败次数
         */
        private Integer failureCnt;
    }

}