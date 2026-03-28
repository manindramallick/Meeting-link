package com.demo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Central Spring configuration class.
 * Enables binding of all {@link TokenProperties} from application.yml.
 */
@Configuration
@EnableConfigurationProperties(TokenProperties.class)
public class AppConfig {
}

