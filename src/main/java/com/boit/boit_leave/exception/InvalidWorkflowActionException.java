package com.boit.boit_leave.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidWorkflowActionException extends RuntimeException {

    public InvalidWorkflowActionException(String message) {
        super(message);
    }
}