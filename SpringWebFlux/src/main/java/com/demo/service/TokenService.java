package com.demo.service;

import com.demo.config.TokenProperties;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final TokenProperties tokenProperties;

    public String generateToken(String env) {
        String name       = tokenProperties.getEnvironment().name();
        String secret     = tokenProperties.getEnvironment().secret();
        String expiration = tokenProperties.getEnvironment().expiration();
        if(!StringUtil.isNullOrEmpty(env) && StringUtil.isNullOrEmpty(name)) {
            return "token_" + env + "_" + secret + "_" + expiration;
        }
        return "token_" + name + "_" + secret + "_" + expiration;
    }
}
