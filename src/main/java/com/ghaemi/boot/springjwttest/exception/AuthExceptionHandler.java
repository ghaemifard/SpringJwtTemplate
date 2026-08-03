package com.ghaemi.boot.springjwttest.exception;

import com.ghaemi.boot.springjwttest.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes =  {AuthController.class})
@RequiredArgsConstructor
public class AuthExceptionHandler {
    private final ExceptionHelper helper;


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return helper.genError(status.value(),
                status.getReasonPhrase(),
                  "Invalid username or password");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(UsernameNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return helper.genError(status.value(),
                status.getReasonPhrase(),
                "Invalid username or password");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return helper.genError(status.value(),
                status.getReasonPhrase(),
                "Invalid username or password");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        var messages = new HashMap<String, String>();
        e.getBindingResult().getFieldErrors().forEach(error -> {messages.put(error.getField(), error.getDefaultMessage());});

        return helper.genError(HttpStatus.BAD_REQUEST.value(), "Validation Failed",messages);

    }

}
