package com.lyon.easy.async.task.protocol.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.lyon.easy.async.task.handler.BatchTaskHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lyon
 */
@SuppressWarnings({"NullableProblems", "rawtypes", "unchecked"})
public class BeanNameTaskHandlerProtocol implements TaskHandlerProtocol, TaskHandlerRegister, BeanDefinitionRegistryPostProcessor, Ordered, ApplicationContextAware {

    private final Map<String, BatchTaskHandler> taskHandlerMap = new HashMap<>();
    private ApplicationContext context;


    @Override
    public void init() {
        taskHandlerMap.putAll(context.getBeansOfType(BatchTaskHandler.class));
    }

    @Override
    public <T> void register(BatchTaskHandler<T> batchTaskHandler) {
        taskHandlerMap.put(batchTaskHandler.getClass().getCanonicalName(), batchTaskHandler);
    }

    @Override
    public <T> BatchTaskHandler<T> getHandler(String taskAddress) {
        BatchTaskHandler<T> batchTaskHandler = null;
        if (support(taskAddress)) {
            final String beanName = StrUtil.removePrefix(taskAddress, TaskProtocols.BEAN);
            batchTaskHandler = taskHandlerMap.get(beanName);
        }
        Assert.notNull(batchTaskHandler, String.format("task handler [%s] not found", taskAddress));
        return batchTaskHandler;
    }

    @Override
    public boolean support(String taskAddress) {
        return StrUtil.startWith(taskAddress, TaskProtocols.BEAN);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        init();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}