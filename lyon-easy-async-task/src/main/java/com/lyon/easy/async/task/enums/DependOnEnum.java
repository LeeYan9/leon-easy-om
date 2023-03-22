package com.lyon.easy.async.task.enums;

import com.lyon.easy.async.task.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lyon
 */

@AllArgsConstructor
@Getter
public enum DependOnEnum {
    /**
     * 依赖类型
     */
    UPSTREAM(0, "依赖上游"),
    DOWNSTREAM(1, "依赖下游"),
    ;
    @EnumValue
    private final int code;
    private final String desc;
}
