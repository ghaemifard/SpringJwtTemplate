package com.ghaemi.boot.springjwttest.exception;

import com.ghaemi.boot.springjwttest.controller.AuthController;
import com.ghaemi.boot.springjwttest.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthExceptionHandler {
    private final ExceptionHelper helper;
    private final ServletUtils servletUtils;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("Authentication failed - bad credentials - path={}, ip={}",
                request.getRequestURI(),
                servletUtils.getClientIP(request));
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return helper.genError(status.value(),
                status.getReasonPhrase(),
                  "Invalid username or password");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(UsernameNotFoundException e, HttpServletRequest request) {
        log.warn("Authentication failed - user not found - path={}, ip={}, message={}",
                request.getRequestURI(),
                servletUtils.getClientIP(request),
                e.getMessage());
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return helper.genError(status.value(),
                status.getReasonPhrase(),
                "Invalid username or password");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.warn("Business error - path={}, ip={}, message={}",
                request.getRequestURI(),
                servletUtils.getClientIP(request),
                e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return helper.genError(status.value(),
                status.getReasonPhrase(),
                e.getMessage() != null ? e.getMessage() : "Bad request");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        var messages = new HashMap<String, String>();

        e.getBindingResult().getFieldErrors().forEach(error -> {messages.put(error.getField(), error.getDefaultMessage());});
        log.warn("Login validation failed - path={}, ip={}, errors={}",
                request.getRequestURI(),
                servletUtils.getClientIP(request),
                messages);
        return helper.genError(HttpStatus.BAD_REQUEST.value(), "Login validation Failed",messages);

    }

}
