package com.lyon.easy.async.task.util;

import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.TaskGroupConfig;
import com.lyon.easy.async.task.config.idc.IdcProperties;
import com.lyon.easy.async.task.core.BatchTaskManager;
import com.lyon.easy.async.task.data.BatchTask;
import com.lyon.easy.async.task.data.SubTask;
import org.hibernate.validator.HibernateValidator;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;

/**
 * @author Lyon
 */
@SuppressWarnings("unused")
public class ParamsCheckerUtil {

    private static final Validator VALIDATOR;

    static {
        VALIDATOR = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .failFast(true)
                .buildValidatorFactory()
                .getValidator();
    }

    public static void check(BatchTask batchTask) {
        validate(batchTask);
    }

    public static void check(BatchTaskManager batchTaskManager) {
        validate(batchTaskManager);
    }

    public static void check(ExecutorConfig executorConfig) {
        validate(executorConfig);
    }

    public static void check(IdcProperties idcProperties) {
        validate(idcProperties);
    }

    public static void check(TaskGroupConfig taskGroupConfig) {
        validate(taskGroupConfig);
    }

    private static <T>void validate(T data , Class<?> ... groups){
        VALIDATOR.validate(data,groups);
    }
}
