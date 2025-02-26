package com.zilch.payment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
@ConfigurationProperties(prefix = "spring.threadpools")
public class AsyncThreadPoolsConfig {

    @Autowired
    private ThreadPoolConfigProperties threadPoolProperties;

    @Bean(name = "verificationSchedulerExecutor")
    public Executor verificationSchedulerExecutor() {
        return createExecutor(threadPoolProperties.getVerificationScheduler());
    }

    @Bean(name = "verificationRunnerExecutor")
    public Executor verificationRunnerExecutor() {
        return createExecutor(threadPoolProperties.getVerificationRunner());
    }

    @Bean(name = "verificationAnalyzerExecutor")
    public Executor verificationAnalyzerExecutor() {
        return createExecutor(threadPoolProperties.getVerificationAnalyzer());
    }

    private Executor createExecutor(ThreadPoolConfigProperties.ThreadPoolProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }
}