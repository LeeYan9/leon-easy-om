package com.lyon.easy.async.task.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.lyon.easy.async.task.annotation.EnumValue;
import com.lyon.easy.async.task.dal.dataobject.task.BatchTaskDO;
import com.lyon.easy.common.utils.CollUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyon
 */

@AllArgsConstructor
@Getter
public enum ExecStatus {
    /**
     * 任务执行状态[-1:失败，0:初始化，1:执行成功,2:运行中，3:部分执行成功，-9：，-15：取消，-20：（任务未执行，执行中被取消）]
     */
    FAILED(-1, "失败"),
    ERROR(-9, "异常"),
    CANCEL(-15, "取消"),
    INTERRUPT(-20, "中断（任务未执行，执行中被取消）"),
    INIT(0, "初始化"),
    SUCCESS(1, "执行成功"),
    RUNNING(2, "运行中"),
    PAIR(3, "部分执行成功"),
    ;

    /**
     * 可加锁的状态
     */
    public static final List<ExecStatus> LOCKABLE_STATUES = Lists.newArrayList(INIT, RUNNING, FAILED);

    /**
     * 可释放锁的状态
     */
    public static final List<ExecStatus> RELEASABLE_STATUES = Lists.newArrayList(FAILED, ERROR, CANCEL, INTERRUPT, INIT, RUNNING, PAIR);

    @EnumValue
    @com.baomidou.mybatisplus.annotation.EnumValue
    private final int code;
    private final String desc;
}