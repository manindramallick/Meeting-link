package com.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

/**
 * Binds all properties under the {@code token} prefix from application.yml.
 *
 * <pre>
 * token:
 *   environment_default: local
 *   environment:
 *     name: dev
 *     secret: secret
 *     expiration: 3600
 * </pre>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    private String environmentDefault;

    private Environment environment;


    public record Environment(
            @DefaultValue("dev")     String name,
            @DefaultValue("secret")  String secret,
            @DefaultValue("3600")    String expiration
    ) {}
}

