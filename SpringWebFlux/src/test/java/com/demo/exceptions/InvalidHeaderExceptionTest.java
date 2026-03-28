package com.demo.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvalidHeaderException")
class InvalidHeaderExceptionTest {

    @Test
    @DisplayName("should store headerName, receivedValue and message correctly")
    void shouldStoreAllFields() {
        InvalidHeaderException ex = new InvalidHeaderException("env", "bad-value", "Custom message");

        assertThat(ex.getHeaderName()).isEqualTo("env");
        assertThat(ex.getReceivedValue()).isEqualTo("bad-value");
        assertThat(ex.getMessage()).isEqualTo("Custom message");
    }

    @Test
    @DisplayName("should allow null receivedValue when header is completely missing")
    void shouldAllowNullReceivedValue() {
        InvalidHeaderException ex = new InvalidHeaderException("env", null, "Header missing");

        assertThat(ex.getHeaderName()).isEqualTo("env");
        assertThat(ex.getReceivedValue()).isNull();
        assertThat(ex.getMessage()).isEqualTo("Header missing");
    }

    @Test
    @DisplayName("should be a RuntimeException subclass")
    void shouldBeRuntimeException() {
        InvalidHeaderException ex = new InvalidHeaderException("env", "val", "msg");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}

