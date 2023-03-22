package com.lyon.easy.async.task.protocol.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.lyon.easy.async.task.handler.TaskHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lyon
 */
@SuppressWarnings("SpringFacetCodeInspection")
public class BeanNameTaskHandlerProtocol implements TaskProtocol, TaskHandlerRegister, ApplicationContextAware, SmartInitializingSingleton {

    private final Map<String, TaskHandler> taskHandlerMap = new HashMap<>();
    private ApplicationContext ac;

    @Override
    public void register(TaskHandler taskHandler) {
        taskHandlerMap.put(taskHandler.getClass().getCanonicalName(), taskHandler);
    }

    @Override
    public TaskHandler getHandler(String taskAddress) {
        TaskHandler taskHandler = null;
        if (support(taskAddress)) {
            final String beanName = StrUtil.removePrefix(taskAddress, TaskProtocols.PROTOCOL);
            taskHandler = taskHandlerMap.get(beanName);
        }
        Assert.notNull(taskHandler);
        return taskHandler;
    }

    @Override
    public boolean support(String taskAddress) {
        return StrUtil.startWith(taskAddress, TaskProtocols.PROTOCOL);
    }

    @Override
    public void afterSingletonsInstantiated() {
        final Map<String, TaskHandler> beansOfType = ac.getBeansOfType(TaskHandler.class);
        beansOfType.forEach(taskHandlerMap::put);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ac = applicationContext;
    }
}
