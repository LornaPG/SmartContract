package com.smartcontract.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50); // Set the core pool size
        executor.setMaxPoolSize(100); // Set the maximum pool size
        executor.setQueueCapacity(500); // Set the queue capacity
        executor.setKeepAliveSeconds(60); // Set the keep alive time
        executor.setThreadNamePrefix("SmartContract-MessageProcessor-"); // Set the thread name prefix
        executor.initialize();
        return executor;
    }
}
