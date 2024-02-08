package com.example.taskmanagementapp.exception;

public class UserIdMismatchException extends RuntimeException{

    public UserIdMismatchException(String message) {
        super(message);
    }
}
