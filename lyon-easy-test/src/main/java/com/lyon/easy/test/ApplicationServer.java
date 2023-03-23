package com.lyon.easy.test;

import cn.hutool.core.net.NetUtil;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.lyon.easy.common.utils.HostUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Lyon
 */
@SpringBootApplication(exclude = {DruidDataSourceAutoConfigure.class, DataSourceAutoConfiguration.class})
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class ApplicationServer {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext run = SpringApplication.run(ApplicationServer.class, args);
        ConfigurableEnvironment env = run.getEnvironment();
        String port = env.getProperty("server.port", "8003");
        log.info("Access URLs:\n----------------------------------------------------------\n\t"
                        + "service {} is started!\n\t"
                        + "Local: \t\thttp://{}:{}\n\t"
                        + "External: \thttp://{}:{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                HostUtils.getHostIp(),
                port,
                NetUtil.getLocalhost().getHostAddress(),
                port
        );
    }
}
