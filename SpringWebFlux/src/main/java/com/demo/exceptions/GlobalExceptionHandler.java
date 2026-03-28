package com.demo.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidHeader(InvalidHeaderException ex) {
        log.error("Header validation failed — header='{}', received='{}', message={}",
                ex.getHeaderName(), ex.getReceivedValue(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp",      Instant.now().toString());
        body.put("status",         HttpStatus.BAD_REQUEST.value());
        body.put("error",          "Invalid Header");
        body.put("header",         ex.getHeaderName());
        body.put("receivedValue",  ex.getReceivedValue());
        body.put("message",        ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

