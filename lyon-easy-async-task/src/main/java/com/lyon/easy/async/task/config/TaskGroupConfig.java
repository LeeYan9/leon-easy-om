package com.lyon.easy.async.task.config;

import com.lyon.easy.async.task.util.ParamsCheckerUtil;
import lombok.Data;

import java.util.List;

/**
 * @author Lyon
 */
@Data
public class TaskGroupConfig {

    private String name;

    private String executorId;

    private List<String> groups;

    private int taskLimit = 20;

    private int execCoreSize = 5;

    private int execMaxSize = 5;

//    private int acquireCoreSize = ;

    private String acquireStrategy = "default";

    private long intervalTimeMills = 1000 * 60 * 5;

    public void init(){
        ParamsCheckerUtil.check(this);
    }

}
