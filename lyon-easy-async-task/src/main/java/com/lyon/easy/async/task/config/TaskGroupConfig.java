package com.lyon.easy.async.task.config;

import com.lyon.easy.async.task.util.ParamsCheckerUtil;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Lyon
 */
@Data
public class TaskGroupConfig {

    /**
     * 执行器名称
     */
    @NotBlank(message = "执行器名称不能为空")
    private String name;

    /**
     * 执行器ID
     */
    private String executorId;

    /**
     * 支持的任务组
     */

    @NotEmpty(message = "执行器名称不能为空")
    private List<String> groups;

    /**
     * 每次拉取的任务数量
     */
    private int taskLimit = 20;

    /**
     * 执行器核心线程数量
     */
    private int execCoreSize = 5;

    /**
     * 执行器最大线程数量
     */
    private int execMaxSize = 5;

    /**
     * 最大故障次数，任务失效次数>={{maxFailureCount}} 时，不再继续处理当前任务
     */
    private Integer maxFailureCount;

    /**
     * 获取任务的间隔时间策略
     */

    @NotBlank(message = "执行器任务间隔策略不能为空")
    private String acquireStrategy = "default";

    /**
     * 初始间隔时间
     */
    private long intervalTimeMills = 1000 * 60 * 5;

    public void init(){
        ParamsCheckerUtil.check(this);
    }

}
