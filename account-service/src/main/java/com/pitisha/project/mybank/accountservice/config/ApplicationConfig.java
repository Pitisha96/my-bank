package com.pitisha.project.mybank.accountservice.config;

import com.pitisha.project.mybank.accountservice.config.properties.ApplicationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfig {
}
