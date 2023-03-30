package com.lyon.easy.async.task.annotation;

import java.lang.annotation.*;

/**
 * mark enum value field
 * @author Lyon
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnumValue {
}
