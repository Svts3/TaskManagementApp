package com.example.taskmanagementapp.exception;

import org.springframework.security.core.AuthenticationException;

public class UserIdMismatchException extends AuthenticationException {

    public UserIdMismatchException(String message) {
        super(message);
    }
}
