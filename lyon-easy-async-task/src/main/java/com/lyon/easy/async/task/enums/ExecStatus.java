package com.lyon.easy.async.task.enums;

import com.google.common.collect.Lists;
import com.lyon.easy.async.task.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author lyon
 */

@AllArgsConstructor
@Getter
public enum ExecStatus {
    /**
     * 任务执行状态[-1:失败，0:初始化，1:执行成功,2:运行中，3:部分执行成功]
     */
    FAILED(-1, "失败"),
    INIT(0, "初始化"),
    SUCCESS(1, "执行成功"),
    RUNNING(2, "运行中"),
    PAIR(3, "部分执行成功"),

    ;

    public static final List<ExecStatus> RUN_STATUES = Lists.newArrayList(SUCCESS,RUNNING,PAIR);

    @EnumValue
    @com.baomidou.mybatisplus.annotation.EnumValue
    private final int code;
    private final String desc;
}