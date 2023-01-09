package com.example.wpct.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("com.example.wpct.service.scheduled")
@EnableScheduling
public class TaskSchedulerConfig {
}
