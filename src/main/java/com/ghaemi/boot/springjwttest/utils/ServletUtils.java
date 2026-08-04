package com.ghaemi.boot.springjwttest.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ServletUtils {
    public String getClientIP(HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        return ip;
    }
}
