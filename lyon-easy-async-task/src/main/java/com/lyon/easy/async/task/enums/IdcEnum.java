package com.lyon.easy.async.task.enums;

import com.lyon.easy.async.task.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lyon
 */

@AllArgsConstructor
@Getter
public enum IdcEnum {
    /**
     * 机房消费类型 [0:任意某个机房消费,1:所有机房都必须消费]
     */
    ANY(0, "任意单机房消费"),
    //        CUSTOM,
    ALL(1, "所有机房消费"),
    ;
    @EnumValue
    private final int code;
    private final String desc;
}