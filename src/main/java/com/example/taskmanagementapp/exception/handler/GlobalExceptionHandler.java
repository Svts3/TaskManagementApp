package com.example.taskmanagementapp.exception.handler;

import com.example.taskmanagementapp.exception.*;
import com.example.taskmanagementapp.model.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleException(AuthenticationException AuthenticationException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(AuthenticationException.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(errorResponse);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleException(RefreshTokenExpiredException refreshTokenExpiredException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(refreshTokenExpiredException.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(errorResponse);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(RefreshTokenNotFoundException refreshTokenNotFoundException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(refreshTokenNotFoundException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(RoleNotFoundException roleNotFoundException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(roleNotFoundException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(TaskNotFoundException taskNotFoundException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(taskNotFoundException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleException(BadCredentialsException badCredentialsException) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(badCredentialsException.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<ErrorResponse> handleException(UserExistException userExistException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(userExistException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(errorResponse);
    }

    @ExceptionHandler(UserIdMismatchException.class)
    public ResponseEntity<ErrorResponse> handleException(UserIdMismatchException userIdMismatchException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(userIdMismatchException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(UserNotFoundException userNotFoundException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(userNotFoundException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(WorkspaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(WorkspaceNotFoundException workspaceNotFoundException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(workspaceNotFoundException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleException(DataIntegrityViolationException dataIntegrityViolationException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(dataIntegrityViolationException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleException(AccessDeniedException accessDeniedException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message("Access denied: You don't have permission to perform this operation.")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserNotInWorkspaceException.class)
    public ResponseEntity<ErrorResponse> handleException(UserNotInWorkspaceException userNotInWorkspaceException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(userNotInWorkspaceException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResponse> handleException(PermissionDeniedException permissionDeniedException) {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(permissionDeniedException.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed: " + errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
