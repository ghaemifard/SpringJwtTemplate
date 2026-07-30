package com.ghaemi.boot.springjwttest.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenDuration;
    private static final String REFRESH_TOKEN_NAME = "ref_token";
    public void addDeleteRefTokenCookie(HttpServletResponse response, String token) {
        token = (token == null) ? "" : token.strip();
        long age = token.length()>0 ? refreshTokenDuration/1000 : 0;
        ResponseCookie cookie =  ResponseCookie.from(REFRESH_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(false)
                .maxAge(age)
                .path("/")
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie",cookie.toString());

    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for(Cookie c : cookies) {
                if(c.getName().equals(REFRESH_TOKEN_NAME))
                    return c.getValue();
            }
        }
        return null;
    }
}
