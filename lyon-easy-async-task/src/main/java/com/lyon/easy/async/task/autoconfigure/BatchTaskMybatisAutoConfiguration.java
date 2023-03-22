package com.lyon.easy.async.task.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.lyon.easy.async.task.config.ExecutorConfig;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

/**
 * @author Lyon
 */
@MapperScan(value = "com.lyon.easy.async.task.dal.mysql",
        sqlSessionFactoryRef = "batchTaskSqlSessionFactory"
)
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
public class BatchTaskMybatisAutoConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "async-task.datasource")
    public DataSource asyncTaskDataSource() {
        return new DruidDataSource();
    }

    @Bean("batchTaskSqlSessionFactory")
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(ResourceLoader resourceLoader,
                                               ExecutorConfig executorConfig,
                                               DataSource dataSource) throws Exception {
        // TODO 使用 MybatisSqlSessionFactoryBean 而不是 SqlSessionFactoryBean
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        MybatisPlusProperties properties = new MybatisPlusProperties();
        final GlobalConfig globalConfig = new GlobalConfig();
        final GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        // todo
        dbConfig.setTablePrefix(executorConfig.getTablePrefix());
        dbConfig.setLogicNotDeleteValue("0");
        dbConfig.setLogicDeleteValue("1");
        globalConfig.setDbConfig(dbConfig);
        properties.setGlobalConfig(globalConfig);
        properties.setMapperLocations(new String[]{"classpath://mapper/task/**"});
        factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        final MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
        factory.setConfiguration(mybatisConfiguration);
        factory.setGlobalConfig(globalConfig);
        return factory.getObject();
    }

    @Bean("batchTaskSqlSessionTemplate")
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(@Autowired @Qualifier("batchTaskSqlSessionFactory")
                                                         SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.SIMPLE);
    }

}
