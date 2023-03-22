package com.lyon.easy.async.task.enums;

import com.lyon.easy.async.task.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lyon
 */

@AllArgsConstructor
@Getter
public enum ExecStatus {
    /**
     * 任务执行状态
     */
    FAILED(-1, "失败"),
    INIT(0, "初始化"),
    SUCCESS(1, "执行成功"),
    RUNNING(2, "运行中"),
    PAIR(3, "部分执行成功"),

    ;
    @EnumValue
    private final int code;
    private final String desc;
}