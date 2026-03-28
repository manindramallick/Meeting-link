package com.demo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a required HTTP header is missing or contains an invalid value.
 */
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidHeaderException extends RuntimeException {

    private final String headerName;
    private final String receivedValue;

    public InvalidHeaderException(String headerName, String receivedValue, String message) {
        super(message);
        this.headerName    = headerName;
        this.receivedValue = receivedValue;
    }

}

