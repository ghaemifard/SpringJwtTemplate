package com.ghaemi.boot.springjwttest.controller;


import com.ghaemi.boot.springjwttest.dto.AuthResponse;
import com.ghaemi.boot.springjwttest.dto.LoginRequest;
import com.ghaemi.boot.springjwttest.entity.RefreshToken;
import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.security.JwtService;
import com.ghaemi.boot.springjwttest.security.RefreshTokenRedisService;
import com.ghaemi.boot.springjwttest.security.RefreshTokenService;
import com.ghaemi.boot.springjwttest.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRedisService refreshTokenRedisService;

    private final CookieUtils cookieUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(),request.password())

        );

        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(user);
        //From Postgresql:
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
//        cookieUtils.addDeleteRefTokenCookie(response, refreshToken.getToken());

        String refreshToken = refreshTokenRedisService.createRefreshToken(user.getUsername());
        cookieUtils.addDeleteRefTokenCookie(response, refreshToken);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String refTokenStr = cookieUtils.extractRefreshToken(request);
        if(refTokenStr==null){
            throw new RuntimeException("Please login again. Invalid cookie");
        }
        // From postgresql
//        RefreshToken refreshToken = refreshTokenService.findByToken(refTokenStr);
//        if(refreshToken==null){
//            throw new RuntimeException("Please login again. Invalid token.");
//        }
//
//        refreshToken = refreshTokenService.verifyRefreshToken(refreshToken);
//
//        UserDetails userDetails = refreshToken.getUser();
//        String newJwtToken = jwtService.generateToken(userDetails);
//
//        refreshTokenService.deleteByToken(refreshToken.getToken());
//        RefreshToken newRefToken   =  refreshTokenService.createRefreshToken(userDetails.getUsername());
//        cookieUtils.addDeleteRefTokenCookie(response, newRefToken.getToken());


        User user = refreshTokenRedisService.findUserByToken(refTokenStr);
        String newJwtToken = jwtService.generateToken(user);

        refreshTokenRedisService.deleteByToken(refTokenStr);
        String newRefreshToken = refreshTokenRedisService.createRefreshToken(user.getUsername());

        cookieUtils.addDeleteRefTokenCookie(response, newRefreshToken);



        return ResponseEntity.ok(new AuthResponse(newJwtToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieUtils.extractRefreshToken(request);
        if(token!=null){
            //From postgresql
//            refreshTokenService.deleteByToken(token);
            refreshTokenRedisService.deleteByToken(token);
        }

        cookieUtils.addDeleteRefTokenCookie(response, null);

        return ResponseEntity.ok("Successfully logged out.");
    }

}