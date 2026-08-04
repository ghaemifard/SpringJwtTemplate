package com.ghaemi.boot.springjwttest.exception;

import com.ghaemi.boot.springjwttest.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    private final ExceptionHelper helper;
    private final ServletUtils servletUtils;
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String,Object>> handleException(Exception e, HttpServletRequest request) {
        log.warn("Global business error - path={}, ip={}, message={}",
                request.getRequestURI(),
                servletUtils.getClientIP(request),
                e.getMessage());
        return helper.genError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Unexpected error occured");
    }
}
