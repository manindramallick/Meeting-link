package com.demo.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("should return 400 with all fields populated for InvalidHeaderException")
    void shouldReturn400WithBody() {
        InvalidHeaderException ex = new InvalidHeaderException("env", "bad-val", "Invalid env header");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidHeader(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Invalid Header");
        assertThat(body.get("header")).isEqualTo("env");
        assertThat(body.get("receivedValue")).isEqualTo("bad-val");
        assertThat(body.get("message")).isEqualTo("Invalid env header");
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("should include timestamp in ISO-8601 format")
    void shouldIncludeTimestamp() {
        InvalidHeaderException ex = new InvalidHeaderException("env", "x", "msg");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidHeader(ex);

        Map<String, Object> body2 = response.getBody();
        assertThat(body2).isNotNull();
        String timestamp = (String) body2.get("timestamp");
        assertThat(timestamp).matches("\\d{4}-\\d{2}-\\d{2}T.*Z");
    }

    @Test
    @DisplayName("should handle null receivedValue gracefully")
    void shouldHandleNullReceivedValue() {
        InvalidHeaderException ex = new InvalidHeaderException("env", null, "Header missing");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidHeader(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body3 = response.getBody();
        assertThat(body3).isNotNull();
        assertThat(body3.get("receivedValue")).isNull();
    }
}

