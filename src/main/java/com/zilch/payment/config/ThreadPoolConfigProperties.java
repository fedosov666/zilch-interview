package com.zilch.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.threadpools")
public class ThreadPoolConfigProperties {
    private ThreadPoolProperties verificationScheduler = new ThreadPoolProperties();
    private ThreadPoolProperties verificationRunner = new ThreadPoolProperties();
    private ThreadPoolProperties verificationAnalyzer = new ThreadPoolProperties();

    @Data
    public class ThreadPoolProperties {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private String threadNamePrefix;
    }
}