package com.lyon.easy.async.task.autoconfigure;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.autoconfigure.*;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.lyon.easy.async.task.config.ExecutorConfig;
import com.lyon.easy.async.task.config.mybatis.DefaultListableTableNameContainer;
import com.lyon.easy.async.task.config.mybatis.handler.AsyncTaskTableNamePrefixHandler;
import com.lyon.easy.common.utils.CollUtils;
import lombok.Data;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.lyon.easy.async.task.config.mybatis.handler.AsyncTaskTableNamePrefixHandler.TableNamePrefix;

/**
 * @author Lyon
 */
@MapperScan(value = "com.lyon.easy.async.task.dal.mysql",
        sqlSessionFactoryRef = "batchTaskSqlSessionFactory"
)
@ConditionalOnBean(ExecutorConfig.class)
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@Data
public class BatchTaskMybatisAutoConfiguration {


    private MybatisPlusProperties properties;

    private Interceptor[] interceptors;

    private TypeHandler[] typeHandlers;

    private LanguageDriver[] languageDrivers;

    private ResourceLoader resourceLoader;

    private DatabaseIdProvider databaseIdProvider;

    private List<ConfigurationCustomizer> configurationCustomizers;

    private List<MybatisPlusPropertiesCustomizer> mybatisPlusPropertiesCustomizers;

    private ApplicationContext applicationContext;


    public BatchTaskMybatisAutoConfiguration(MybatisPlusProperties properties,
                                             ObjectProvider<TypeHandler[]> typeHandlersProvider,
                                             ObjectProvider<LanguageDriver[]> languageDriversProvider,
                                             ResourceLoader resourceLoader,
                                             ObjectProvider<DatabaseIdProvider> databaseIdProvider,
                                             ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
                                             ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider,
                                             ApplicationContext applicationContext) {
        this.properties = properties;
        this.typeHandlers = typeHandlersProvider.getIfAvailable();
        this.languageDrivers = languageDriversProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
        this.mybatisPlusPropertiesCustomizers = mybatisPlusPropertiesCustomizerProvider.getIfAvailable();
        this.applicationContext = applicationContext;
    }


    @Bean("asyncTaskDataSource")
    @ConfigurationProperties(prefix = "async-task.datasource.druid")
    public DataSource asyncTaskDataSource() {
        return DruidDataSourceBuilder.create().build();
    }


    @Bean("asyncTaskTableNamePrefixHandler")
    public TableNameHandler asyncTaskTableNamePrefixHandler(ExecutorConfig executorConfig) {
        return new AsyncTaskTableNamePrefixHandler(new TableNamePrefix(executorConfig.getTablePrefix()));
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(@Autowired @Qualifier("asyncTaskTableNamePrefixHandler")
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

    @Bean
    public TableNameHandler asyncTaskTableNameHandler(ExecutorConfig executorConfig) {
        return new AsyncTaskTableNamePrefixHandler(new TableNamePrefix(executorConfig.getTablePrefix()));
    }

    @Bean("batchTaskSqlSessionFactory")
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(@Autowired @Qualifier("asyncTaskDataSource") DataSource dataSource, List<Interceptor> interceptors) throws Exception {
        this.interceptors = ArrayUtil.toArray(interceptors, Interceptor.class);
        // TODO 使用 MybatisSqlSessionFactoryBean 而不是 SqlSessionFactoryBean
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        // todo
        dbConfig.setLogicNotDeleteValue("0");
        dbConfig.setLogicDeleteValue("1");
        globalConfig.setDbConfig(dbConfig);
        // TODO 使用 MybatisSqlSessionFactoryBean 而不是 SqlSessionFactoryBean
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(this.properties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }
        applyConfiguration(factory);
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (this.properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
        }
        if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(this.typeHandlers)) {
            factory.setTypeHandlers(this.typeHandlers);
        }
        Resource[] mapperLocations = this.properties.resolveMapperLocations();
        if (!ObjectUtils.isEmpty(mapperLocations)) {
            factory.setMapperLocations(mapperLocations);
        }
        // TODO 修改源码支持定义 TransactionFactory
        this.getBeanThen(TransactionFactory.class, factory::setTransactionFactory);

//        // TODO 对源码做了一定的修改(因为源码适配了老旧的mybatis版本,但我们不需要适配)
//        Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
//        if (!ObjectUtils.isEmpty(this.languageDrivers)) {
//            factory.setScriptingLanguageDrivers(this.languageDrivers);
//        }
//        Optional.ofNullable(defaultLanguageDriver).ifPresent(factory::setDefaultScriptingLanguageDriver);

        // TODO 自定义枚举包
//        if (StringUtils.hasLength(this.properties.getTypeEnumsPackage())) {
//            factory.setTypeEnumsPackage(this.properties.getTypeEnumsPackage());
//        }
        // TODO 此处必为非 NULL
        // TODO 注入填充器
        this.getBeanThen(MetaObjectHandler.class, globalConfig::setMetaObjectHandler);
        // TODO 注入主键生成器
        this.getBeanThen(IKeyGenerator.class, i -> globalConfig.getDbConfig().setKeyGenerator(i));
        // TODO 注入sql注入器
        this.getBeanThen(ISqlInjector.class, globalConfig::setSqlInjector);
        // TODO 注入ID生成器
        this.getBeanThen(IdentifierGenerator.class, globalConfig::setIdentifierGenerator);
        // TODO 设置 GlobalConfig 到 MybatisSqlSessionFactoryBean
        factory.setGlobalConfig(globalConfig);
        return factory.getObject();
    }

    // TODO 入参使用 MybatisSqlSessionFactoryBean
    private void applyConfiguration(MybatisSqlSessionFactoryBean factory) {
        // TODO 使用 MybatisConfiguration
        MybatisConfiguration configuration = this.properties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(this.properties.getConfigLocation())) {
            configuration = new MybatisConfiguration();
        }
        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
    }

    /**
     * 检查spring容器里是否有对应的bean,有则进行消费
     *
     * @param clazz    class
     * @param consumer 消费
     * @param <T>      泛型
     */
    private <T> void getBeanThen(Class<T> clazz, Consumer<T> consumer) {
        if (this.applicationContext.getBeanNamesForType(clazz, false, false).length > 0) {
            consumer.accept(this.applicationContext.getBean(clazz));
        }
    }

    @Bean("batchTaskSqlSessionTemplate")
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(@Autowired @Qualifier("batchTaskSqlSessionFactory")
                                                         SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.SIMPLE);
    }

}
