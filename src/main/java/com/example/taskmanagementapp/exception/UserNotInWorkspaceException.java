package com.example.taskmanagementapp.exception;

public class UserNotInWorkspaceException extends RuntimeException {

    public UserNotInWorkspaceException(String message) {
        super(message);
    }
}
