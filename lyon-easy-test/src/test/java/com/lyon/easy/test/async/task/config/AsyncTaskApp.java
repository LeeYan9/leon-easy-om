package com.lyon.easy.test.async.task.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Lyon
 */
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true, mode = AdviceMode.ASPECTJ)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class AsyncTaskApp {
}
