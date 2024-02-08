package com.example.taskmanagementapp.exception;

public class WorkspaceNotFoundException extends RuntimeException{

    public WorkspaceNotFoundException(String message) {
        super(message);
    }
}
