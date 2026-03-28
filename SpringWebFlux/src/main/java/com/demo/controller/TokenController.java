package com.demo.controller;

import com.demo.annotation.ValidateEnvHeader;
import com.demo.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    @ValidateEnvHeader(requireCorrelationId = false)
    @GetMapping("/token")
    public ResponseEntity<?> getToken(
            @RequestHeader(value = "env", required = false) String env) {
        log.info("TokenController: generating token for env={}", env);
        return ResponseEntity.ok("Generated token: " + tokenService.generateToken(env));
    }
}
