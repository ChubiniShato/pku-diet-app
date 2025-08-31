package com.chubini.pku.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    log.info("Creating Async Task Executor");

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("PKU-Async-");
    executor.setRejectedExecutionHandler(
        (r, e) -> {
          log.warn("Task rejected, thread pool is full and queue is also full");
        });
    executor.initialize();
    return executor;
  }
}
