package com.lyon.easy.async.task.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.mybatis.DefaultListableTableNameContainer;
import com.lyon.easy.async.task.config.mybatis.handler.AsyncTaskTableNamePrefixHandler;
import com.lyon.easy.async.task.config.mybatis.inject.BatchInsertSqlInject;
import com.lyon.easy.common.utils.CollUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * @author Lyon
 */
@ConditionalOnBean(ExecutorConfig.class)
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class AsyncBatchTaskMybatisAutoConfigure implements InitializingBean {

    @Bean("asyncBatchTaskTableNamePrefixHandler")
    public TableNameHandler asyncTaskTableNamePrefixHandler(ExecutorConfig executorConfig) {
        return AsyncTaskTableNamePrefixHandler.builder().prefix(executorConfig.getTablePrefix()).build();
    }

    @Bean
    public ISqlInjector batchInsertSqlInject(){
        return new BatchInsertSqlInject();
    }
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(@Autowired
                                                         @Qualifier("asyncBatchTaskTableNamePrefixHandler")
                                                                 TableNameHandler tableNameHandler) {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁插件
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 动态表名
        final Map<String, TableNameHandler> tableNameHandlerMap = CollUtils.convertMap(
                DefaultListableTableNameContainer.getTableNames(), key -> key, key -> tableNameHandler);
        mybatisPlusInterceptor.addInnerInterceptor(new DynamicTableNameInnerInterceptor(tableNameHandlerMap));
        return mybatisPlusInterceptor;
    }

    @Override
    public void afterPropertiesSet() {
        // do something .
    }
}
