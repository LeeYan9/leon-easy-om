package com.lyon.easy.async.task.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.lyon.easy.async.task.core.BatchTaskManager;
import com.lyon.easy.async.task.BatchTaskTemplate;
import com.lyon.easy.async.task.DefaultBatchTaskTemplate;
import com.lyon.easy.async.task.core.idc.IdcContainer;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.idc.IdcProperties;
import com.lyon.easy.async.task.factory.DefaultTaskHandlerFactory;
import com.lyon.easy.async.task.factory.TaskHandlerFactory;
import com.lyon.easy.async.task.protocol.idc.IdcProtocol;
import com.lyon.easy.async.task.protocol.idc.PrefixMatchingIdcProtocol;
import com.lyon.easy.async.task.protocol.task.TaskProtocol;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author Lyon
 */
@ConditionalOnBean(ExecutorConfig.class)
@EnableConfigurationProperties({IdcProperties.class})
@AutoConfigureAfter(MybatisPlusAutoConfiguration.class)
public class BatchTaskAutoConfiguration {


    @ConditionalOnMissingBean
    @Bean("init")
    public IdcProtocol idcProtocol(IdcProperties idcProperties) {
        final PrefixMatchingIdcProtocol protocol = new PrefixMatchingIdcProtocol();
        protocol.setIdcProperties(idcProperties);
        return protocol;
    }

    @ConditionalOnMissingBean
    @Bean
    public IdcContainer idcContainer(IdcProtocol idcProtocol) {
        return new IdcContainer(idcProtocol);
    }

    @ConditionalOnMissingBean
    @Bean
    public TaskHandlerFactory taskHandlerFactory(List<TaskProtocol> taskProtocols) {
        return new DefaultTaskHandlerFactory(taskProtocols);
    }

    @ConditionalOnMissingBean
    @Bean(initMethod = "init")
    public BatchTaskManager batchTaskManager(ExecutorConfig executorConfig,
                                             IdcContainer idcContainer,
                                             TaskHandlerFactory taskHandlerFactory) {
        return new BatchTaskManager(executorConfig, idcContainer, taskHandlerFactory);
    }


    @ConditionalOnMissingBean
    @Bean
    public BatchTaskTemplate batchTaskTemplate(BatchTaskManager batchTaskManager) {
        return new DefaultBatchTaskTemplate(batchTaskManager);
    }

}
